package com.seeyon.v3x.edoc.constants;

import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;

public final class EdocNavigationEnum {

	public enum RegisterRetreatState {
		NotRetreat, Retreated
	}
	
	/*public enum RegisterState {
		DraftBox, WaitRegister, Registed, retreat, All
	}*/
	public enum RegisterState {
		DraftBox, WaitRegister, Registed, Register_StepBacked, retreat, deleted, All
	}
	
	public enum EdocDistributeState {
		DraftBox, WaitDistribute, Distributed,Distribute_Back
	}
	
	public enum EdocRetreat {
		edocExSendRetreat, //Affair发文分发退件箱标识 
		edocRecieveRetreat, //Affair收文签收退件箱标识 
		edocRegisterRetreat, //Affair收文登记退件箱标识
		edocDistributeRetreat //Affair收文登记退件箱标识
	}
	
	public enum RegisterType {
		All, ByAutomatic, ByManual, ByCode
	}
	
	public enum RecieveFromType {
		All, Inner, Outer
	}
	
	public enum RecieveDateType {
		All, Today, LastDay, ThisWeek, LastWeek, ThisMonth, LastMonth, ThisYear, LastYear
	}	
	
	public enum RegisterListState {
		listRegister("listRegister", 1),
    	registerPending("registerPending", 1),
        registerByAutomatic("registerByAutomatic", 1),
        registerByManual("registerByManual", 1),
        registerByCode("registerByCode", 1),		
		registerDraft("registerDraft", 0),
		registerDraftByAutomatic("registerDraftByAutomatic", 0),
		registerDraftByManual("registerDraftByManual", 0),
		registerDraftByCode("registerDraftByCode", 0),
        registerDone("registerDone", 2),
        registerDoneByAutomatic("registerDoneByAutomatic", 2),
        registerDoneByManual("registerDoneByManual", 2),
        registerRetreat("registerRetreat", RegisterState.retreat.ordinal()),
        registerDoneByCode("registerDoneByCode", 2);        
        
        String key;
        int value;
        
        RegisterListState(String key, int value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        public int getValue() {
            return value;
        }        
        /**
         * 获取key表示的意义
         * @param key 键
         * @return key表示的意义
         */
        public static int getDisplayName(String key) {
            return getEnumByKey(key).getValue();
        }

        /**
         * 根据key获取枚举
         * @param key 键
         * @return TypeFlagEnum
         */
        public static RegisterListState getEnumByKey(String key) {
            for(RegisterListState e : RegisterListState.values()) {
                if(e.getKey().equals(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
        }
    }
    
    public static enum RegisterListType {
    	
    	registerDraft("registerDraft", 0),
		registerDraftByAutomatic("registerDraftByAutomatic", 1),
		registerDraftByManual("registerDraftByManual", 2),
		registerDraftByCode("registerDraftByCode", 3),
    	listRegister("listRegister", 0),
    	registerPending("registerPending", 0),
    	registerDone("registerDone", 0),
        registerByAutomatic("registerByAutomatic", 1),
        registerByManual("registerByManual", 2),
        registerByCode("registerByCode", 3),
        registerDoneByAutomatic("registerDoneByAutomatic", 1),
        registerDoneByManual("registerDoneByManual", 2),
        registerRetreat("registerRetreat", 0),
        registerDoneByCode("registerDoneByCode", 3);
        
        String key;
        int value;
        
        RegisterListType(String key, int value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        public int getValue() {
            return value;
        }    
        
        /**
         * 获取key表示的意义
         * @param key 键
         * @return key表示的意义
         */
        public static int getDisplayName(String key) {
            return getEnumByKey(key).getValue();
        }

        /**
         * 根据key获取枚举
         * @param key 键
         * @return TypeFlagEnum
         */
        public static RegisterListType getEnumByKey(String key) {
            for(RegisterListType e : RegisterListType.values()) {
                if(e.getKey().equals(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
        }
    }    
    
    public enum RecieveFromListType {
    	toReceive(TORECEIVE, 0),
    	retreat("retreat",0),
    	listV5Register("listV5Register", 0),
    	listRecieveRetreat("listRecieveRetreat", 0),
    	toReceiveFromInner("toReceiveFromInner", 1),
    	toReceiveFromInnerToday("toReceiveFromInnerToday", 1),
    	toReceiveFromInnerLastDay("toReceiveFromInnerLastDay", 1),
    	toReceiveFromInnerThisWeek("toReceiveFromInnerThisWeek", 1),		
    	toReceiveFromInnerLastWeek("toReceiveFromInnerLastWeek", 1),	
    	toReceiveFromInnerThisMonth("toReceiveFromInnerThisMonth", 1),	
    	toReceiveFromInnerLastMonth("toReceiveFromInnerLastMonth", 1),	
    	toReceiveFromInnerThisYear("toReceiveFromInnerThisYear", 1),	
    	toReceiveFromInnerLastYear("toReceiveFromInnerLastYear", 1),	
    	toReceiveFromOuter("toReceiveFromOuter", 2),	
    	toReceiveFromOuterToday("toReceiveFromOuterToday", 2),		
    	toReceiveFromOuterLastDay("toReceiveFromOuterLastDay", 2),	
    	toReceiveFromOuterThisWeek("toReceiveFromOuterThisWeek", 2),	
    	toReceiveFromOuterLastWeek("toReceiveFromOuterLastWeek", 2),	
    	toReceiveFromOuterThisMonth("toReceiveFromOuterThisMonth", 2),
    	toReceiveFromOuterLastMonth("toReceiveFromOuterLastMonth", 2),
    	toReceiveFromOuterThisYear("toReceiveFromOuterThisYear", 2),
    	toReceiveFromOuterLastYear("toReceiveFromOuterLastYear", 2),        
    	received(RECEIVED, 0),
    	receivedFromInner("receivedFromInner", 1),
    	receivedFromInnerToday("receivedFromInnerToday", 1),
    	receivedFromInnerLastDay("receivedFromInnerLastDay", 1),
    	receivedFromInnerThisWeek("receivedFromInnerThisWeek", 1),		
    	receivedFromInnerLastWeek("receivedFromInnerLastWeek", 1),	
    	receivedFromInnerThisMonth("receivedFromInnerThisMonth", 1),	
    	receivedFromInnerLastMonth("receivedFromInnerLastMonth", 1),	
    	receivedFromInnerThisYear("receivedFromInnerThisYear", 1),	
    	receivedFromInnerLastYear("receivedFromInnerLastYear", 1),	
    	receivedFromOuter("receivedFromOuter", 2),	
    	receivedFromOuterToday("receivedFromOuterToday", 2),		
    	receivedFromOuterLastDay("receivedFromOuterLastDay", 2),	
    	receivedFromOuterThisWeek("receivedFromOuterThisWeek", 2),	
    	receivedFromOuterLastWeek("receivedFromOuterLastWeek", 2),	
    	receivedFromOuterThisMonth("receivedFromOuterThisMonth", 2),
    	receivedFromOuterLastMonth("receivedFromOuterLastMonth", 2),
    	receivedFromOuterThisYear("receivedFromOuterThisYear", 2),
    	receivedFromOuterLastYear("receivedFromOuterLastYear", 2);    
    	
        String key;
        int value;
        
        RecieveFromListType(String key, int value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        public int getValue() {
            return value;
        }        
        /**
         * 获取key表示的意义
         * @param key 键
         * @return key表示的意义
         */
        public static int getDisplayName(String key) {
            return getEnumByKey(key).getValue();
        }

        /**
         * 根据key获取枚举
         * @param key 键
         * @return TypeFlagEnum
         */
        public static RecieveFromListType getEnumByKey(String key) {
            for(RecieveFromListType e : RecieveFromListType.values()) {
                if(e.getKey().equals(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
        }
    }   
    
    public enum RecieveDateListType {
    	toReceive(TORECEIVE, 0),
    	retreat("retreat", 0),
    	listV5Register("listV5Register", 0),
    	listRecieveRetreat("listRecieveRetreat", 0),
    	toReceiveFromInner("toReceiveFromInner", 0),
    	toReceiveFromInnerToday("toReceiveFromInnerToday", 1),
    	toReceiveFromInnerLastDay("toReceiveFromInnerLastDay", 2),
    	toReceiveFromInnerThisWeek("toReceiveFromInnerThisWeek", 3),		
    	toReceiveFromInnerLastWeek("toReceiveFromInnerLastWeek", 4),	
    	toReceiveFromInnerThisMonth("toReceiveFromInnerThisMonth", 5),	
    	toReceiveFromInnerLastMonth("toReceiveFromInnerLastMonth", 6),	
    	toReceiveFromInnerThisYear("toReceiveFromInnerThisYear", 7),	
    	toReceiveFromInnerLastYear("toReceiveFromInnerLastYear", 8),	
    	toReceiveFromOuter("toReceiveFromOuter", 0),	
    	toReceiveFromOuterToday("toReceiveFromOuterToday", 1),		
    	toReceiveFromOuterLastDay("toReceiveFromOuterLastDay", 2),	
    	toReceiveFromOuterThisWeek("toReceiveFromOuterThisWeek", 3),	
    	toReceiveFromOuterLastWeek("toReceiveFromOuterLastWeek", 4),	
    	toReceiveFromOuterThisMonth("toReceiveFromOuterThisMonth", 5),
    	toReceiveFromOuterLastMonth("toReceiveFromOuterLastMonth", 6),
    	toReceiveFromOuterThisYear("toReceiveFromOuterThisYear", 7),
    	toReceiveFromOuterLastYear("toReceiveFromOuterLastYear", 8),
    	received(RECEIVED, 0),
    	receivedFromInner("receivedFromInner", 0),
    	receivedFromInnerToday("receivedFromInnerToday", 1),
    	receivedFromInnerLastDay("receivedFromInnerLastDay", 2),
    	receivedFromInnerThisWeek("receivedFromInnerThisWeek", 3),		
    	receivedFromInnerLastWeek("receivedFromInnerLastWeek", 4),	
    	receivedFromInnerThisMonth("receivedFromInnerThisMonth", 5),	
    	receivedFromInnerLastMonth("receivedFromInnerLastMonth", 6),	
    	receivedFromInnerThisYear("receivedFromInnerThisYear", 7),	
    	receivedFromInnerLastYear("receivedFromInnerLastYear", 8),	
    	receivedFromOuter("receivedFromOuter", 0),	
    	receivedFromOuterToday("receivedFromOuterToday", 1),		
    	receivedFromOuterLastDay("receivedFromOuterLastDay", 2),	
    	receivedFromOuterThisWeek("receivedFromOuterThisWeek", 3),	
    	receivedFromOuterLastWeek("receivedFromOuterLastWeek", 4),	
    	receivedFromOuterThisMonth("receivedFromOuterThisMonth", 5),
    	receivedFromOuterLastMonth("receivedFromOuterLastMonth", 6),
    	receivedFromOuterThisYear("receivedFromOuterThisYear", 7),
    	receivedFromOuterLastYear("receivedFromOuterLastYear", 8);    
        
        String key;
        int value;
        
        RecieveDateListType(String key, int value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        public int getValue() {
            return value;
        }        
        /**
         * 获取key表示的意义
         * @param key 键
         * @return key表示的意义
         */
        public static int getDisplayName(String key) {
            return getEnumByKey(key).getValue();
        }

        /**
         * 根据key获取枚举
         * @param key 键
         * @return TypeFlagEnum
         */
        public static RecieveDateListType getEnumByKey(String key) {
            for(RecieveDateListType e : RecieveDateListType.values()) {
                if(e.getKey().equals(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
        }
    }   
    public final static String TORECEIVE = "toReceive";
    public final static String RECEIVED = "received";
    public enum RecieveModelListType {
    	toReceive(TORECEIVE, TORECEIVE),
    	retreat("retreat","retreat"),
    	listV5Register("listV5Register", RECEIVED),
    	listRecieveRetreat("listRecieveRetreat","retreat"),
    	toReceiveFromInner("toReceiveFromInner", TORECEIVE),
    	toReceiveFromInnerToday("toReceiveFromInnerToday", TORECEIVE),
    	toReceiveFromInnerLastDay("toReceiveFromInnerLastDay", TORECEIVE),
    	toReceiveFromInnerThisWeek("toReceiveFromInnerThisWeek", TORECEIVE),		
    	toReceiveFromInnerLastWeek("toReceiveFromInnerLastWeek", TORECEIVE),	
    	toReceiveFromInnerThisMonth("toReceiveFromInnerThisMonth", TORECEIVE),	
    	toReceiveFromInnerLastMonth("toReceiveFromInnerLastMonth", TORECEIVE),	
    	toReceiveFromInnerThisYear("toReceiveFromInnerThisYear", TORECEIVE),	
    	toReceiveFromInnerLastYear("toReceiveFromInnerLastYear", TORECEIVE),	
    	toReceiveFromOuter("toReceiveFromOuter", TORECEIVE),	
    	toReceiveFromOuterToday("toReceiveFromOuterToday", TORECEIVE),		
    	toReceiveFromOuterLastDay("toReceiveFromOuterLastDay", TORECEIVE),	
    	toReceiveFromOuterThisWeek("toReceiveFromOuterThisWeek", TORECEIVE),	
    	toReceiveFromOuterLastWeek("toReceiveFromOuterLastWeek", TORECEIVE),	
    	toReceiveFromOuterThisMonth("toReceiveFromOuterThisMonth", TORECEIVE),
    	toReceiveFromOuterLastMonth("toReceiveFromOuterLastMonth", TORECEIVE),
    	toReceiveFromOuterThisYear("toReceiveFromOuterThisYear", TORECEIVE),
    	toReceiveFromOuterLastYear("toReceiveFromOuterLastYear", TORECEIVE),    
    	received(RECEIVED, RECEIVED),
    	receivedFromInner("receivedFromInner", RECEIVED),
    	receivedFromInnerToday("receivedFromInnerToday", RECEIVED),
    	receivedFromInnerLastDay("receivedFromInnerLastDay", RECEIVED),
    	receivedFromInnerThisWeek("receivedFromInnerThisWeek", RECEIVED),		
    	receivedFromInnerLastWeek("receivedFromInnerLastWeek", RECEIVED),	
    	receivedFromInnerThisMonth("receivedFromInnerThisMonth", RECEIVED),	
    	receivedFromInnerLastMonth("receivedFromInnerLastMonth", RECEIVED),	
    	receivedFromInnerThisYear("receivedFromInnerThisYear", RECEIVED),	
    	receivedFromInnerLastYear("receivedFromInnerLastYear", RECEIVED),	
    	receivedFromOuter("receivedFromOuter", RECEIVED),	
    	receivedFromOuterToday("receivedFromOuterToday", RECEIVED),		
    	receivedFromOuterLastDay("receivedFromOuterLastDay", RECEIVED),	
    	receivedFromOuterThisWeek("receivedFromOuterThisWeek", RECEIVED),	
    	receivedFromOuterLastWeek("receivedFromOuterLastWeek", RECEIVED),	
    	receivedFromOuterThisMonth("receivedFromOuterThisMonth", RECEIVED),
    	receivedFromOuterLastMonth("receivedFromOuterLastMonth", RECEIVED),
    	receivedFromOuterThisYear("receivedFromOuterThisYear", RECEIVED),
    	receivedFromOuterLastYear("receivedFromOuterLastYear", RECEIVED); 
        String key;
        String value;
        
        RecieveModelListType(String key, String value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }        
        /**
         * 获取key表示的意义
         * @param key 键
         * @return key表示的意义
         */
        public static String getDisplayName(String key) {
            return getEnumByKey(key).getValue();
        }

        /**
         * 根据key获取枚举
         * @param key 键
         * @return TypeFlagEnum
         */
        public static RecieveModelListType getEnumByKey(String key) {
            for(RecieveModelListType e : RecieveModelListType.values()) {
                if(e.getKey().equals(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
        }
    }

    public static final int LIST_TYPE_PENDING = 10;
    public static final int LIST_TYPE_DONE = 20;
    public static final int LIST_TYPE_WAIT_SEND = 30;
    public static final int LIST_TYPE_SENT = 40;
    public static final int LIST_TYPE_READ = 50;
    public static final int LIST_TYPE_REGISTER = 60;
    public static final int LIST_TYPE_FENFA = 70;
    public static final int LIST_TYPE_DISTRIBUTE = 80;
    public static final int LIST_TYPE_EX_SEND = 90;
    public static final int LIST_TYPE_EX_RECIEVE = 100;
    public static final int LIST_STATE_ALL = -1;
    public static final int LIST_SUBSTATE_STATE_ALL = -1;
    
    /**
     * 每个列表的类型(dao中区分)
     * @author tangguiling
     *
     */
    public enum EdocV5ListTypeEnum {
    	/** 待办  */
    	listPending("listPending", LIST_TYPE_PENDING, 
    			String.valueOf(StateEnum.col_pending.key()),
    			SubStateEnum.col_normal.getKey()+","
    			+SubStateEnum.col_waitSend_stepBack.getKey()+","
    			+SubStateEnum.col_pending_unRead.getKey()+","
    			+SubStateEnum.col_pending_read.getKey()+","
    			+SubStateEnum.col_pending_assign.getKey()+","
    			+SubStateEnum.col_pending_specialBack.getKey()+","
    			+SubStateEnum.col_pending_specialBacked.getKey()+","
    			+SubStateEnum.col_pending_takeBack.getKey()+","
    			+SubStateEnum.col_pending_specialBackCenter.getKey()+","
    			+SubStateEnum.col_waitSend_sendBack.getKey()+","
    			,
    			"edocListController.do",
    			"listPending"),
    			
    	/** 在办  */
    	listZcdb("listZcdb", LIST_TYPE_PENDING, 
    			String.valueOf(StateEnum.col_pending.key()),
    			String.valueOf(SubStateEnum.col_pending_ZCDB.key())+","
    			+String.valueOf(SubStateEnum.col_pending_specialBackToSenderReGo.getKey()),
    			"edocListController.do",
    			"listZcdb"),
    			
    	/** 所有待办  */    	
    	listPendingAll("listPendingAll", LIST_TYPE_PENDING, 
    			String.valueOf(StateEnum.col_pending.key()),
    			SubStateEnum.col_normal.getKey()+","
    			+SubStateEnum.col_waitSend_stepBack.getKey()+","
    			+SubStateEnum.col_pending_unRead.getKey()+","
    			+SubStateEnum.col_pending_read.getKey()+","
    			+SubStateEnum.col_pending_assign.getKey()+","
    			+SubStateEnum.col_pending_specialBack.getKey()+","
    			+SubStateEnum.col_pending_specialBacked.getKey()+","
    			+SubStateEnum.col_pending_specialBackCenter.getKey()+","
    	    	+SubStateEnum.col_pending_takeBack.getKey()+","
    	    	+SubStateEnum.col_pending_Back.getKey()+","
    			+SubStateEnum.col_pending_ZCDB.key(),
    			"edocListController.do",
    			"listPending"),
    	
    	/** 未查看 */
    	listNotLook("listNotLook", LIST_TYPE_PENDING,
    		     String.valueOf(StateEnum.col_pending.key()),
    		     String.valueOf(SubStateEnum.col_pending_unRead.getKey()),
    		     "edocListController.do",
    		     "listNotLook"),		
    			
    	/** 未办结 */
    	listDone("listDone", LIST_TYPE_DONE, 
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocListController.do",
    			"listDone"),
    	
    	/** 已办结 */
    	listFinish("listFinish", LIST_TYPE_DONE, 
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocListController.do", 
    			"listDone"),
    			
    	/** 所有已办 */
    	listDoneAll("listDoneAll", LIST_TYPE_DONE, 
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocListController.do",
    			"listDone"),
    	
    	/** 待发 */
    	listWaitSend("listWaitSend", LIST_TYPE_WAIT_SEND, 
    			String.valueOf(StateEnum.col_waitSend.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocListController.do",
    			"listWaitSend"),
    	
    	/** 退件箱 */
		backBox("backBox", LIST_TYPE_WAIT_SEND, 
				String.valueOf(StateEnum.col_waitSend.key()),
				SubStateEnum.col_waitSend_sendBack.getKey()+","+SubStateEnum.col_waitSend_stepBack.getKey(),
				"edocListController.do",
				"listWaitSend"),
		
		/** 退件箱 */
		retreat("retreat", LIST_TYPE_WAIT_SEND, 
				String.valueOf(StateEnum.col_waitSend.key()),
				SubStateEnum.col_waitSend_sendBack.getKey()+","+SubStateEnum.col_waitSend_stepBack.getKey(),
				"edocListController.do",
				"listWaitSend"),
		
		/** 草稿箱 */
		draftBox("draftBox", LIST_TYPE_WAIT_SEND,
				String.valueOf(StateEnum.col_waitSend.key()),
				SubStateEnum.col_waitSend_draft.getKey()+","+SubStateEnum.col_waitSend_cancel.getKey(),
				"edocListController.do",
				"listWaitSend"),

		/** 已发 */		
    	listSent("listSent", LIST_TYPE_SENT, 
    			String.valueOf(StateEnum.col_sent.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocListController.do",
    			"listSent"),
    
    	/** 待阅*/
    	listReading("listReading", LIST_TYPE_READ,
    			String.valueOf(StateEnum.col_pending.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"listReading"),
    			
    	/** 已阅 */
    	listReaded("listReaded", LIST_TYPE_READ, 
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"listReaded"),
    			
    	/** 发文登记簿 */
    	sendRegister("sendRegister", LIST_TYPE_REGISTER,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"sendRegister"),
    	
    	/** 收文登记簿 */
    	recRegister("recRegister", LIST_TYPE_REGISTER,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"recRegister"),
    			
    	/** 发文分发 */
    	listFenfa("listFenfa", LIST_TYPE_FENFA, 
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"exchangeEdoc.do",
    			"list"),
    			
    	/** 交换待发送 */
    	listExchangeToSend("listExchangeToSend", LIST_TYPE_EX_SEND,
    			String.valueOf(StateEnum.col_pending.key()),
    			String.valueOf(EdocSendRecord.Exchange_iStatus_Tosend+","+EdocSendRecord.Exchange_iStatus_Send_New_StepBacked+","+EdocSendRecord.Exchange_iStatus_Send_New_Cancel),
    			"exchangeEdoc.do",
    			"list"),

		/** 交换已发送 */
    	listExchangeSent("listExchangeSent", LIST_TYPE_EX_SEND,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(EdocSendRecord.Exchange_iStatus_Sent+","+EdocSendRecord.Exchange_iStatus_Send_StepBacked),
    			"exchangeEdoc.do",
    			"list"),    			
    			
    	/** 收文分发 */
    	listDistribute("listDistribute", LIST_TYPE_DISTRIBUTE,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"listDistribute"),
    	
    	/** 收文签收 */
    	listRecieve("listRecieve", LIST_TYPE_EX_RECIEVE,
    			String.valueOf(StateEnum.col_pending.key()),
    			String.valueOf(EdocRecieveRecord.Exchange_iStatus_Torecieve),
    			"exchangeEdoc.do",
    			"listRecieve"),

    	/** 交换待签收 */
    	listExchangeToRecieve("listExchangeToRecieve", LIST_TYPE_EX_RECIEVE,
    			String.valueOf(StateEnum.col_pending.key()),
    			String.valueOf(EdocRecieveRecord.Exchange_iStatus_Torecieve),
    			"exchangeEdoc.do",
    			"list"),

		/** 交换已签收 */
    	listExchangeReceived("listExchangeReceived", LIST_TYPE_EX_RECIEVE,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(EdocRecieveRecord.Exchange_iStatus_Recieved+","+EdocRecieveRecord.Exchange_iStatus_Registered
    			        +","+EdocRecieveRecord.Exchange_iStatus_RegisterToWaitSend),
    			"exchangeEdoc.do",
    			"list"),

    	/** 收文登记(G6) */
    	listRegister("listRegister", LIST_TYPE_REGISTER,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"listRegister"),
    	
    	/** 收文新建登记(G6） */
    	newEdocRegister("newEdocRegister", LIST_TYPE_REGISTER,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"newEdocRegister"),
    	
    	/** 收文待登记 */
    	listV5Register("listV5Register", LIST_TYPE_REGISTER,
    			String.valueOf(StateEnum.col_done.key()),
    			String.valueOf(EdocRecieveRecord.Exchange_iStatus_Recieved),
    			"exchangeEdoc.do",
    			"listV5Register"),
    	
    	/** 收文分发 */
    	newEdoc("newEdoc", LIST_TYPE_DISTRIBUTE,
    			String.valueOf(StateEnum.col_sent.key()),
    			String.valueOf(LIST_SUBSTATE_STATE_ALL),
    			"edocController.do",
    			"newEdoc");
    	
    	String key;
        int type;
        String state;
        String substate;
        String controller;
        String method;
        
        EdocV5ListTypeEnum(String key, int value, String state, String substate, String controller, String method) {
            this.key = key;
            this.type = value;
            this.state = state;
            this.substate = substate;
            this.controller = controller;
            this.method = method;
        }
        
        public String getKey() {
            return this.key;
        }
        
        public int getType() {
            return this.type;
        }   
        
        public String getState() {
            return this.state;
        }    
        
        public String getSubstate() {
            return this.substate;
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
        public static int getTypeName(String key) {
            return getEnumByKey(key).getType();
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
        public static EdocV5ListTypeEnum getEnumByKey(String key) {
            for(EdocV5ListTypeEnum e : EdocV5ListTypeEnum.values()) {
                if(e.getKey().equals(key)) {
                    return e;
                }
            }
            throw new IllegalArgumentException(ResourceUtil.getString("metdata.manager.undefined")+"key=" + key);//未定义的枚举类型!
        }
    }
    
    /**
     * 
     * @author tangguiling
     *
     */
    public enum EdocListCombType {
    	Comb_No, Comb_Yes
    }
    
    /**
     * 是否组合查询
     * @author tangguiling
     *
     */
    public enum EdocListCombTypeEnum {
    	Comb_No, Comb_Yes 
    }
    
    /**
     * 阅文/办文
     * @author tangguiling
     *
     */
    public enum EdocProcessTypeEnum {
    	ProcessType_All, ProcessType_Done, ProcessType_Read;
    }
}
