package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.constants.RecRelationAfterSendParam;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.util.ExchangeUtil;

public class RecRelationHandlerByG6 extends RecRelationHandlerDefault{

	private static final Log log = LogFactory.getLog(RecRelationHandlerByG6.class);
	private EdocRegisterManager edocRegisterManager = (EdocRegisterManager)AppContext.getBean("edocRegisterManager");
	private AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
	private RecieveEdocManager recieveEdocManager = (RecieveEdocManager)AppContext.getBean("recieveEdocManager");
	private EdocSummaryManager edocSummaryManager = (EdocSummaryManager)AppContext.getBean("edocSummaryManager");
	
	
	@Override
	public void transAfterSaveRec(EdocSummary summary, EdocRegister register,
			String recieveId,String comm) {
		if(summary!=null && summary.getEdocType()==1 && "distribute".equals(comm)){
			if(register!=null){
				register.setDistributeState(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());//分发草稿
				register.setDistributeEdocId(summary.getId());
        		edocRegisterManager.update(register);
        		
        		//当不是V5自动登记时，也就是更新的G6的待分发affair为已办
        		
        			//wangjingjing 更新分发待办事件 begin
        		Object[][] wheres = new Object[4][2];
        		wheres[0] = new Object[]{"app", ApplicationCategoryEnum.edoc.getKey()};
        		wheres[1] = new Object[]{"subApp", ApplicationSubCategoryEnum.old_edocRecDistribute.getKey()};
        		wheres[2] = new Object[]{"objectId", register.getId()};
        		wheres[3] = new Object[]{"memberId", register.getDistributerId()};
        		Map columns = new HashMap();
        		//这里affair app为34分发，保存后state应该为4，不然首页待发栏目会显示两条，因为会显示收文20的
        		columns.put("state", StateEnum.col_done.getKey());
        		columns.put("subObjectId", summary.getId());
        		columns.put("delete", true);//分发后，原交换的分发待办数据状态修改为删除，不在已办中显示
        		try {
					affairManager.update(columns, wheres);
				} catch (BusinessException e) {
					log.error("G6收文保存待发affairManager.update方法报错!", e);
				}
        		
			}
		}
		
		
		/**
    	 * 因为新建纸质收文保存后，edoc_register登记表中是没有数据的，而分发待发中 是需要显示 签收人的，edoc_summary关联edoc_register才能查出
		 * 但这样关联查询后，纸质收文保存待发的 就查不出来了
		 * 解决方案： 新建纸质收文保存后，生成登记数据，登记状态为一个新的状态，是不在登记列表中显示的
    	 */
		if(summary!=null && summary.getEdocType()==1){
			ExchangeUtil.createRegisterDataByPaperEdoc(summary, edocRegisterManager);
    	}
		
		//如果G6版本 登记数据是自动登记的，那么收文分发后，对应的签收状态要变为已分发，这个需要在已签收列表中展现的
		if(register!= null && Integer.valueOf(EdocRegister.AUTO_REGISTER).equals(register.getAutoRegister())){
			
			EdocRecieveRecord rr = recieveEdocManager.getEdocRecieveRecord(register.getRecieveId());
			try {
				if(rr !=null){
					rr.setReciveEdocId(register.getDistributeEdocId());
					recieveEdocManager.update(rr);
				}
			} catch (BusinessException e) {
				log.error("G6收文保存待发时recieveEdocManager.update方法报错!", e);
			}
		}
		
	}

	@Override
	public void transAfterSendRec(RecRelationAfterSendParam param) {
		EdocSummary summary = param.getSummary();
		EdocRegister register = param.getRegister();
		if(summary.getEdocType() == 1 && register != null){
			register.setDistributeDate(new java.sql.Date(new Date().getTime()));//分发时间
			register.setDistributeState(EdocNavigationEnum.EdocDistributeState.Distributed.ordinal());//将状态设置为"已分发"
			register.setDistributeEdocId(summary.getId());
			edocRegisterManager.update(register);
			// 添加Edoc_Summary中process_type状态
			summary.setProcessType(param.getProcessType());
			edocSummaryManager.saveOrUpdateEdocSummary(summary);
			//wangjingjing 更新分发待办事件 begin
			Object[][] wheres = new Object[4][2];
			wheres[0] = new Object[]{"app", ApplicationCategoryEnum.edoc.getKey()};
			wheres[1] = new Object[]{"subApp", ApplicationSubCategoryEnum.old_edocRecDistribute.getKey()};
			wheres[2] = new Object[]{"objectId", register.getId()};
			wheres[3] = new Object[]{"memberId", register.getDistributerId()};
			Map columns = new HashMap();
			columns.put("state", StateEnum.col_done.getKey());
			columns.put("subObjectId", summary.getId());
			columns.put("delete", true);//分发后，原交换的分发待办数据状态修改为删除，不在已办中显示
			try {
				affairManager.update(columns, wheres);
			} catch (BusinessException e) {
				log.error("G6收文发送时affairManager.update方法报错!", e);
			}
	        //wangjingjing end
			
			//如果G6版本 登记数据是自动登记的，那么收文分发后，对应的签收状态要变为已分发，这个需要在已签收列表中展现的
			recordAddDistributedEdocId(register);
		}
		super.createRegisterDataByPaperEdoc(summary, edocRegisterManager);
	}

	private void recordAddDistributedEdocId(EdocRegister edocRegister){
		if(edocRegister.getAutoRegister() != null && edocRegister.getAutoRegister().intValue() == EdocRegister.AUTO_REGISTER){
			EdocRecieveRecord rr = recieveEdocManager.getEdocRecieveRecord(edocRegister.getRecieveId());
			if(rr != null){
			    rr.setReciveEdocId(edocRegister.getDistributeEdocId());
	            try {
	                recieveEdocManager.update(rr);
	            } catch (BusinessException e) {
	                log.error("G6收文发送时recieveEdocManager.update方法报错!", e);
	            }
			}
		}
	}
	
}
