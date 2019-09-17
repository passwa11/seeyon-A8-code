package com.seeyon.apps.govdoc.constant;

import com.seeyon.apps.govdoc.constant.GovdocEnum.OldExchangeNodePolicyEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.apps.edoc.constants.EdocConstant;

public class GovdocListEnum {

	public enum GovdocListConditionEnum {
		choose,//列表小查询
		com,//列表高级查询
		query,//公文查询
		register,//公文登记簿
		byUrl//URL参数查询
	}
	
	public enum GovdocListConfigTypeEnum {
		pending(1),
		done(2);
		
		int key;
		GovdocListConfigTypeEnum(int key) {
	    	this.key = key;
	    }
	}
	
	public enum GovdocListTypeEnum {
		/** 待办列表-全部  */
    	listPendingAllRoot("listPendingAllRoot", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"", 
    			"", "",
    			"list.do",
    			"listPending"),
    	
    	/** 待办列表-待办  */
    	listPendingRoot("listPendingRoot", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"", "zhihui,yuedu,inform",
    			"list.do",
    			"listPending"),
    	
    	/** 待办列表-待阅  */
    	listReadingRoot("listReadingRoot", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"zhihui,yuedu,inform", "",
    			"list.do",
    			"listPending"),
    	
    	/** 已办列表-全部  */
    	listDoneAllRoot("listDoneAllRoot", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 已办列表-在办  */
    	listDoneRoot("listDoneRoot", "",
    			EdocConstant.flowState.run.ordinal()+"", 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 已办列表-已办结  */
    	listFinishedRoot("listFinishedRoot", "",
    			EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 已发列表  */
    	listSentAllRoot("listSentAllRoot", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_sent.key()),
    			"",
    			"", "",
    			"list.do",
    			"listSent"),
    	
    	/** 待发列表  */
    	listWaitSendAllRoot("listWaitSendAllRoot", "",
    			String.valueOf(EdocConstant.flowState.cancel.ordinal() + "," + EdocConstant.flowState.run.ordinal())+","+EdocConstant.flowState.oldexchange.ordinal(),
    			String.valueOf(StateEnum.col_waitSend.key()),
    			"",
    			"", "",
    			"list.do",
    			"listWaitSend"),
    	
    	/** 所有待办(发文管理/收文管理/签报管理)(待办+办文暂存待办+待阅+阅文暂存待办)  */
    	listPendingAll("listPendingAll", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"", "",
    			"list.do",
    			"listPending"),
    	
		/** 所有已办(发文管理/收文管理/签报管理)(在办[未办结数据]+已办结)  */
    	listDoneAll("listDoneAll", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 待办(发文管理/收文管理/签报管理)(待办+办文暂存待办)  */
    	listPending("listPending", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"", "zhihui,yuedu,inform",
    			"list.do",
    			"listPending"),
    	
    	/** 待办(发文管理/收文管理/签报管理)(待阅+阅文暂存待办)  */
    	listReading("listReading", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"zhihui,yuedu,inform", "",
    			"list.do",
    			"listPending"),
    	
    	/** 已办(发文管理/收文管理/签报管理)  */
    	listDone("listDone", "",
    			String.valueOf(EdocConstant.flowState.run.ordinal()),
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 已办结(发文管理/收文管理/签报管理)  */
    	listFinished("listFinished", "",
    			EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 已发(发文管理/收文管理/签报管理) */
    	listSent("listSent", "",
    			EdocConstant.flowState.run.ordinal() + "," + EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal()+","+EdocConstant.flowState.oldexchange.ordinal(), 
    			String.valueOf(StateEnum.col_sent.key()),
    			"",
    			"", "",
    			"list.do",
    			"listSent"),
    	
    	/** 待发(发文管理/收文管理/签报管理)  */
    	listWaitSend("listWaitSend", "",
    			String.valueOf(EdocConstant.flowState.cancel.ordinal() + "," + EdocConstant.flowState.run.ordinal())+","+EdocConstant.flowState.oldexchange.ordinal(),
    			String.valueOf(StateEnum.col_waitSend.key()),
    			"",
    			"", "",
    			"list.do",
    			"listWaitSend"),
    	
    	/** 业务生成器-待办   */
    	listPendingAllBiz("listPendingAllBiz", "3", 
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"", "",
    			"list.do",
    			"listPending"),
    	
    	/** 业务生成器-已办   */
    	listDoneAllBiz("listDoneAllBiz", "3", 
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listDone"),
    	
    	/** 业务生成器-已发   */
    	listSentAllBiz("listSentAllBiz", "3", 
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_sent.key()),
    			"",
    			"", "",
    			"list.do",
    			"listSent"),
    	
    	/** 业务生成器-待发   */
    	listWaitSendAllBiz("listWaitSendAllBiz", "3", 
    			String.valueOf(EdocConstant.flowState.cancel.ordinal() + "," + EdocConstant.flowState.run.ordinal())+","+EdocConstant.flowState.oldexchange.ordinal(),
    			String.valueOf(StateEnum.col_waitSend.key()),
    			"",
    			"", "",
    			"list.do",
    			"listWaitSend"),
    	
    	/** 待分送    */
    	listExchangeSendPending("listExchangeSendPending", "1",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"FaDistribute," + OldExchangeNodePolicyEnum.oldfasong.name(), "",
    			"list.do",
    			"listExchangeSend"),
    	
    	/** 已分送  */
    	listExchangeSendDone("listExchangeSendDone", "1",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"", "",
    			"list.do",
    			"listExchangeSend"),
    	
    	/** 待签收   */
    	listExchangeSignPending("listExchangeSignPending", "4",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_pending.key()),
    			"",
    			"ReSign," + OldExchangeNodePolicyEnum.oldqianshou.name(), "",
    			"list.do",
    			"listExchangeSign"),
    	
    	/** 已签收   */
    	listExchangeSignDone("listExchangeSignDone", "4",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_done.key()),
    			"",
    			"ReSign," + OldExchangeNodePolicyEnum.oldqianshou.name(), "",
    			"list.do",
    			"listExchangeSign"),
    	
    	/** 已回退   */
    	listExchangeFallback("listExchangeFallback", "4",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			String.valueOf(StateEnum.col_sent.key()),
    			"",
    			"", "",
    			"list.do",
    			"listExchangeFallback"),
    	
    	/** 公文查询   */
    	listQuery("listQuery", "",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			StateEnum.col_sent.key() + "," + StateEnum.col_pending.key() + "," + StateEnum.col_done.key(),
    			"",
    			"", "",
    			"list.do",
    			"queryResult"),
		
    	/** 关联公文   */
    	list4Quote("list4Quote", "",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			StateEnum.col_sent.key() + "," + StateEnum.col_pending.key() + "," + StateEnum.col_done.key(),
    			"",
    			"", "",
    			"list.do",
    			"list4Quote"),
    	
		/** 发文登记簿   */
    	listSendRegister("listSendRegister", "1",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			"",
    			"",
    			"", "",
    			"list.do",
    			"listSendRegister"),
		
    	/** 收文登记簿   */
    	listRecRegister("listRecRegister", "2,4",
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			"",
    			"",
    			"", "",
    			"list.do",
    			"listRecRegister"),
    	
    	/** 签报登记簿   */
    	listSignRegister("listSignRegister", "3", 
    			EdocConstant.flowState.run.ordinal()+","+EdocConstant.flowState.finish.ordinal()+","+EdocConstant.flowState.terminate.ordinal(), 
    			"",
    			"",
    			"", "",
    			"list.do",
    			"listSignRegister");
    	
		String key;
		String govdocType;
		String flowState;
	    String state;
	    String substate;
	    String nodePolicy;
	    String notInNodePolicy;
	    String controller;
	    String method;
	    
	    GovdocListTypeEnum(String key, String govdocType, String flowState, String state, String substate, String nodePolicy, String notInNodePolicy, String controller, String method) {
	        this.key = key;
	        this.govdocType = govdocType;
	        this.flowState = flowState;
	        this.state = state;
	        this.nodePolicy = nodePolicy;
	        this.notInNodePolicy = notInNodePolicy;
	        this.substate = substate;
	        this.controller = controller;
	        this.method = method;
	    }
	    
	    public String getKey() {
	        return this.key;
	    }
	    
	    public String getGovdocType() {
	        return this.govdocType;
	    }
	    
	    public String getFlowState() {
	        return this.flowState;
	    }   
	    
	    public String getState() {
	        return this.state;
	    }    
	    
	    public String getSubstate() {
	        return this.substate;
	    }    
	    
	    public String getNodePolicy() {
	        return this.nodePolicy;
	    }   
	    
	    public String getNotInNodePolicy() {
	        return this.notInNodePolicy;
	    }   
	    
	    public String getController() {
	    	return this.controller;
	    }
	    
	    public String getMethod() {
	    	return this.method;
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getGovdocTypeName(String key) {
	        return getEnumByKey(key).getGovdocType();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getFlowStateName(String key) {
	        return getEnumByKey(key).getFlowState();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getStateName(String key) {
	        return getEnumByKey(key).getState();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getSubStateName(String key) {
	        return getEnumByKey(key).getSubstate();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getNodePolicyName(String key) {
	        return getEnumByKey(key).getNodePolicy();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getNotInNodePolicyName(String key) {
	    	return getEnumByKey(key).getNotInNodePolicy();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getControllerName(String key) {
	        return getEnumByKey(key).getController();
	    }
	    
	    /**
	     * 获取key表示的意义
	     * @param key 键
	     * @return key表示的意义
	     */
	    public static String getMethodName(String key) {
	        return getEnumByKey(key).getMethod();
	    }
	    
	    /**
	     * 根据key获取枚举
	     * @param key 键
	     * @return TypeFlagEnum
	     */
	    public static GovdocListTypeEnum getEnumByKey(String key) {
	        for(GovdocListTypeEnum e : GovdocListTypeEnum.values()) {
	            if(e.getKey().equals(key)) {
	                return e;
	            }
	        }
	        throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
	    }
	
	}
	
}
