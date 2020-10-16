/**
 * Id: EdocManagerFacadeImpl.java, v1.0 2012-4-9 wangchw Exp
 * Copyright (c) 2011 Seeyon, Ltd. All rights reserved
 */
package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.apps.govdoc.bo.DateSharedWithWorkflowEngineThreadLocal;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;

/**
 * @Project/Product: 产品或项目名称（A8）
 * @Description: 类功能描述
 * @Copyright: Copyright (c) 2012 of Seeyon, Ltd.
 * @author: wangchw
 * @time: 2012-4-9 上午11:06:09
 * @version: v1.0
 */
public class EdocManagerFacadeImpl implements EdocManagerFacade {
	
	private static final Log logger = LogFactory.getLog(EdocManagerFacadeImpl.class);
	
	private EdocManager edocManager;
	
	private EdocSuperviseManager edocSuperviseManager;
	
	private RecieveEdocManager recieveEdocManager;
	
	private IndexApi indexApi;

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.edoc.manager.EdocManagerFacade#runCaseFacade()
	 */
	@Override
	public void runCaseFacade(EdocSummary edocSummary,EdocBody body,
			EdocOpinion senderOninion,EdocEnum.SendType sendType,
			Map<String, Object> options,String comm,Long agentToId,
			boolean track,String trackMembers,String trackRange,
			String title,String superviseTitle,String supervisorId,
			String supervisors,String awakeDate,String exchangeIdStr,
			boolean isCanBeRegisted,User user) throws Exception {
		boolean isNew = edocSummary.isNew();

        Long affairId = 0L;
        try {
            affairId = edocManager.runCase(edocSummary, body, senderOninion, sendType, options,comm,agentToId);
        } catch (Exception e) {
            logger.error("发起公文流程异常",e);
        }
        //跟踪
        //不跟踪 或者 全部跟踪的时候不向部门跟踪表中添加数据，所以将下面这个参数串设置为空。
        if(!track || "1".equals(trackRange)) trackMembers = "";
        edocManager.setTrack(affairId, track, trackMembers);

        if(supervisorId != null && !"".equals(supervisorId) && awakeDate != null && !"".equals(awakeDate)) {
	        //boolean canModifyAwake = "on".equals(request.getParameter("canModifyAwake"))?true:false;
	        if(Strings.isBlank(title)){
	        	title = superviseTitle;
	        }
	        Date date = Datetimes.parse(awakeDate, Datetimes.dateStyle);
	        String[] idsStr = supervisorId.split(",");
	        long[] ids = new long[idsStr.length];
	        int i = 0;
	        for(String id:idsStr) {
	        	ids[i] = Long.parseLong(id);
	        	i++;
	        }
	        edocSuperviseManager.superviseForTemplate("100", supervisorId, supervisors, awakeDate,edocSummary,title);
        }else if(supervisorId==null||"".equals(supervisorId)){//如果为空，删除督办。
        	if(edocSummary!=null&&edocSummary.getId()!=null){
        		edocSuperviseManager.deleteSuperviseDetailAndSupervisors(edocSummary);
        	}
        }

        //来文登记,更新登记时间，给签收人发送消息
        if("register".equals(comm) && exchangeIdStr!=null && !"".equals(exchangeIdStr))
        {
			if (isCanBeRegisted) {
				Long exchangeId = Long.parseLong(exchangeIdStr);
				recieveEdocManager.registerRecieveEdoc(exchangeId, edocSummary.getId());
			}
        }

        //通知全文检索不入库
        DateSharedWithWorkflowEngineThreadLocal.setNoIndex();
		//全文检索入库
        if(AppContext.hasPlugin("index")){
   		 try {
   		     indexApi.add(edocSummary.getId(), ApplicationCategoryEnum.edoc.getKey());
			 } catch (BusinessException e) {
				logger.warn(e.getMessage());
			 }
   	    }
	}

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.edoc.manager.EdocManagerFacade#runCaseImmediateFacade()
	 */
	/*图片@Override
	public boolean runCaseImmediateFacade(String _affairId,EdocSummary edocSummary,FlowData flowData) throws Exception{
		DateSharedWithWorkflowEngineThreadLocal.setColSummary(edocSummary);
		edocManager.sendImmediate(Long.parseLong(_affairId),edocSummary, flowData);
		boolean sentFlag = true;
		//全文检索
        DateSharedWithWorkflowEngineThreadLocal.setNoIndex();
        //全文检索入库
        if(IndexInitConfig.hasLuncenePlugIn()){
			try {
				indexManager.index(((IndexEnable)edocManager).getIndexInfo(edocSummary.getId()));
			}catch (Exception e) {
				logger.warn(e.getMessage());
			}
		}
        return sentFlag;
	}*/

	/* (non-Javadoc)
	 * @see com.seeyon.v3x.edoc.manager.EdocManagerFacade#finishWorkItemFacade()
	 */
/*	@Override
	public String finishWorkItemFacade(String supervisorNames,String spMemberId,
			String superviseDate,String processId,String edocMangerID,
			EdocSummary summary,User user,Long affairId,EdocOpinion signOpinion,
			Map<String, String[]> map,Map<String,String> condition,
			String isDeleteSupervisior,String title) throws Exception {
		String ret = null;
        if(null!=supervisorNames && !"".equals(supervisorNames) && null!=spMemberId && !"".equals(spMemberId) && null!=superviseDate && !"".equals(superviseDate)){
        	ret = edocManager.transFinishWorkItem(summary, affairId, signOpinion, map,condition, title, spMemberId, supervisorNames,
        	        superviseDate, processId, user.getId()+"", edocMangerID,null,null);
        } else {
			ret = edocManager.transFinishWorkItem(summary, affairId,signOpinion, map, condition, processId, user.getId()+ "", 
			        edocMangerID,null,null);
			if ("true".equals(isDeleteSupervisior)) {
				edocSuperviseManager.deleteSuperviseDetailAndSupervisors(summary);
			}
        }
//        indexManager.update(summary.getId(),EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).getKey());
          indexManager.update(summary.getId(),EdocUtil.getAppCategoryByEdocType(summary.getEdocType()).getKey());
        return ret;
	}*/

	/**
	 * @param recieveEdocManager the recieveEdocManager to set
	 */
	public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
		this.recieveEdocManager = recieveEdocManager;
	}

	/**
	 * @param edocManager the edocManager to set
	 */
	public void setEdocManager(EdocManager edocManager) {
		this.edocManager = edocManager;
	}

	/**
	 * @param edocSuperviseManager the edocSuperviseManager to set
	 */
	public void setEdocSuperviseManager(EdocSuperviseManager edocSuperviseManager) {
		this.edocSuperviseManager = edocSuperviseManager;
	}

	/**
	 * @param indexManager the indexManager to set
	 */
	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }

	@Override
	public boolean runCaseImmediateFacade(String _affairId,
			EdocSummary edocSummary) throws Exception {
		
		return false;
	}
	
	

}
