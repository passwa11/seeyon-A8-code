package com.seeyon.v3x.edoc.manager;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.appLog.AppLogAction;
import com.seeyon.ctp.common.appLog.manager.AppLogManager;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.constants.RecRelationAfterSendParam;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.util.ExchangeUtil;

public class RecRelationHandlerByA8 extends RecRelationHandlerDefault{

	private static final Log LOGGER = LogFactory.getLog(RecRelationHandlerByA8.class);
	private AppLogManager appLogManager = (AppLogManager)AppContext.getBean("appLogManager");
	private EdocRegisterManager edocRegisterManager = (EdocRegisterManager)AppContext.getBean("edocRegisterManager");
	private RecieveEdocManager recieveEdocManager = (RecieveEdocManager)AppContext.getBean("recieveEdocManager");
	private AffairManager affairManager = (AffairManager)AppContext.getBean("affairManager");
	private EdocManager edocManager = (EdocManager)AppContext.getBean("edocManager");
	
	@Override
	public void transAfterSaveRec(EdocSummary summary, EdocRegister register,
			String recieveId,String comm) {
		//更新登记数据中的 收文id
		if(summary!=null && summary.getEdocType()==1 && ("register".equals(comm)|| "distribute".equals(comm))){
			if(register!=null){
				register.setDistributeState(EdocNavigationEnum.EdocDistributeState.DraftBox.ordinal());//分发草稿
				register.setDistributeEdocId(summary.getId());
				edocRegisterManager.update(register);
			}
		}
		
		if(summary.getEdocType()==1 && Strings.isNotBlank(recieveId)){
			
            EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(recieveId));
            //设置已签收记录 为登记保存待发状态, 保存待发设置已登记
            record.setStatus(EdocRecieveRecord.Exchange_iStatus_Registered);
            
            //OA-33696 yanl对交换过来的公文进行登记，保存待发后再次编辑，正文类型可以修改了
            //将收文id 与发文的签收数据关联，因为保存待发时，登记表中还没有插入数据，那么待发编辑时，只有通过收文找是否有关联的签收数据，来判断正文类型是否可以修改了
            record.setReciveEdocId(summary.getId());
            try {
				recieveEdocManager.update(record);
			} catch (BusinessException e) {
				LOGGER.error("A8收文保存待发recieveEdocManager.update方法报错!", e);
			}
            
          //OA-33822收文待登记的公文，保存待发，然后在编辑发送后，首页待办栏目中待登记的数据没有消失，还存在。
            //下面这个段代码来至于recieveEdocManager.registerRecieveEdoc方法中的跟新ctp_affair表数据
            Map<String, Object> columns=new Hashtable<String, Object>();
            columns.put("state",Integer.valueOf(StateEnum.col_done.getKey()));
        
            try {
				affairManager.update(columns, new Object[][]{{"objectId",Long.valueOf(record.getEdocId())}, {"subObjectId",record.getId()}});
			} catch (BusinessException e) {
				LOGGER.error("A8收文保存待发affairManager.update方法报错!", e);
			}
        }
		if(summary!=null && summary.getEdocType()==1){
			ExchangeUtil.createRegisterDataByPaperEdoc(summary, edocRegisterManager);
    	}
		
	}
	@Override
	public void transAfterSendRec(RecRelationAfterSendParam param) {
		EdocSummary summary = param.getSummary();
		EdocRegister register = param.getRegister();
		String recieveId = param.getRecieveId();
		String waitRegister_recieveId = param.getWaitRegister_recieveId();
		if(1==summary.getEdocType() && register != null){
			if (Strings.isNotBlank(recieveId)) {
				Long exchangeId = Long.parseLong(recieveId);
				try {
					recieveEdocManager.registerRecieveEdoc(exchangeId,summary.getId());
				} catch (Exception e) {
					LOGGER.error("A8收文发送调用recieveEdocManager.registerRecieveEdoc报错!", e);
				}
			}
			//当从待发编辑 发送时，waitRegister_recieveId为空的，而recieveIdStr是有值的
			if(Strings.isNotBlank(waitRegister_recieveId)){
				EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecord(Long.parseLong(waitRegister_recieveId));
				
                //OA-8105 收文登记簿，收文单中有来文单位，但是收文登记簿查询不出来，显示为空。
                register.setRecieveId(Long.parseLong(waitRegister_recieveId));
                register.setSendUnit(record.getSendUnit());
                
                //OA-25318 收文登记簿，1、发文单位和来文单位有什么区别？来文单位一直都是空的，查不出来。 2、抄送单位也是空的。发文的时候又抄送单位，但是到收文登记簿这里查不出来。 
                //3、会签人，不管是发文有会签人，还是新建收文选了会签人，也是查不出来。
                //收文单的发文单位sentTo 对应发文单中的发文单位(联合发文为两个发文单位)
                
                try {
					edocManager.update(summary);
				} catch (Exception e) {
					LOGGER.error("A8收文发送调用edocManager.update报错!", e);
				}
            }
			register.setDistributeDate(new java.sql.Date(new Date().getTime()));
			register.setDistributeState(EdocNavigationEnum.EdocDistributeState.Distributed.ordinal());//分发草稿
			register.setIsRetreat(EdocNavigationEnum.RegisterRetreatState.NotRetreat.ordinal());
			register.setDistributeEdocId(summary.getId());
    		edocRegisterManager.update(register);
		}
		//应用日志
		appLogManager.insertLog(param.getUser(), AppLogAction.Edoc_RegEdoc, param.getUser().getName(),summary.getSubject());
		super.createRegisterDataByPaperEdoc(summary, edocRegisterManager);
	}
	
	@Override
	public String getRecieveIdBeforeSendRec(EdocSummary summary,String recieveIdStr,String waitRegister_recieveId,boolean isNewSent){
		if(Strings.isBlank(recieveIdStr)){
            recieveIdStr = waitRegister_recieveId;
        }
        
        //OA-40052 待登记的公文，在待办栏目中处理，然后保存待发，在待发中编辑，发送后，收文登记簿统计不出来这条公文。
        //表示先保存待发的
        if(!isNewSent){
            //待登记公文电子登记时保存待发后，从待发中编辑发送，需要获得签收id,这样才能按照登记后直接发送的逻辑来走
            EdocRecieveRecord record = recieveEdocManager.getEdocRecieveRecordByReciveEdocId(summary.getId());
            if(record != null){
                recieveIdStr = String.valueOf(record.getId());
            }
        }
		return recieveIdStr;
	}

}
