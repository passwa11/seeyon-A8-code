/**
 * 
 */
package com.seeyon.apps.govdoc.bo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.Strings;


/**
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2008-10-15
 */
public class DateSharedWithWorkflowEngineThreadLocal {
	private static ThreadLocal<Integer> dateSharedWithWorkflowEngine = new ThreadLocal<Integer>();
	
	/**
	 * 处理协同时，意见的id，用于消息的锚点和意见Object[Long(意见的id), boolean(是否隐藏), String(意见的内容)，Integer(态度)]
	 */
	private static ThreadLocal<Object[]> finishWorkitemOpinion = new ThreadLocal<Object[]>();
	
	/**
	 * 发起协同时，ColSummary/EdocSummary对象
	 */
	private static ThreadLocal<Object> colSummaryTL = new ThreadLocal<Object>();
	/**
	 * 是否需要调用全文检索入库，默认true
	 */
	private static ThreadLocal<Boolean> isNeedIndex = new ThreadLocal<Boolean>();
	
	
	/**
	 * 是否需要调用全文检索入库，默认true
	 */
	private static ThreadLocal<Boolean> isProcessFinished = new ThreadLocal<Boolean>();
	
	/**
	 * 是否是竞争执行
	 */
	private static ThreadLocal<String> isProcessCompetion= new ThreadLocal<String>();
	
	public static String getIsProcessCompetion() {
		return isProcessCompetion.get();
	}

	public static void setIsProcessCompetion(String isProcessCompetion2) {
		isProcessCompetion.set(isProcessCompetion2);
	}

	/**
	 * 是否需要自动跳过
	 */
	private static ThreadLocal<Boolean> isNeedAutoSkip = new ThreadLocal<Boolean>();
	
	private static ThreadLocal<Map<String,Object>> traceDataMap  = new ThreadLocal<Map<String,Object>>();
	
	
	private static  ThreadLocal<Map<String,String>> repeatAffairs  = new ThreadLocal<Map<String,String>>();

	private static  ThreadLocal<Map<String,String>> repealAtt  = new ThreadLocal<Map<String,String>>();
   

	/**
     * 记录当前终止流程的Affair <br>
     * 目前用于解决流程终止后有代理人的消息提醒问题
     */
    private static ThreadLocal<CtpAffair> theStopAffairTL = new ThreadLocal<CtpAffair>();
    
    /**
     * 回退时：新产生的Affair，不发回退消息，只发待办消息
     */
    private static ThreadLocal<Map<Long, Long>> affairMap = new ThreadLocal<Map<Long,Long>>();
    
    /**
     * 回退的时候所有影响的affair。包括上一节点，兄弟。
     */
    private static ThreadLocal<Map<Long,Long>> allStepBackAffectAffairMap = new ThreadLocal<Map<Long,Long>>();
    /**
     * 回退的时候所有影响的affair。memeberId和activityId对应affair用来更新signet_htm_document表中对应的记录
     */
    private static ThreadLocal<Map<String,CtpAffair>> memberIdAndActivityId2AffairMap = new ThreadLocal<Map<String,CtpAffair>>();
    
    /**
     * 指定回退的时候所有影响的affair，包括上一节点，兄弟。
     */
    private static ThreadLocal<Map<Long,Long[]>> allSepcialStepBackCanceledAffairMap = new ThreadLocal<Map<Long,Long[]>>();
    
    /**
     * 处理时的affairId，目前只用于公文的onProcessFinished方法
     */
    private static ThreadLocal<Long> finishAffairId = new ThreadLocal<Long>();
    
    /**
     * 交换流程回退时，发送者的affair
     */
    private static ThreadLocal<Map<Long,Long>> affairDistributeMap = new ThreadLocal<Map<Long,Long>>();
    /**
     * 协同或者公文处理时候推送消息
     */
    private static ThreadLocal<List<Long[]>> pushMessageMembers = new ThreadLocal<List<Long[]>>();
    
    /**
     * 当前处理的用户信息。例如当前处理人，代理人信息。
     */
    private static ThreadLocal<Long[]>  currentUserData= new ThreadLocal<Long[]>();
    
    
    private static ThreadLocal<List<CtpAffair>> workflowAssignedAllAffairs = new ThreadLocal<List<CtpAffair>>();
    
	public static List<CtpAffair> getWorkflowAssignedAllAffairs() {
		List<CtpAffair> affairs = workflowAssignedAllAffairs.get();
		return affairs == null ? new ArrayList<CtpAffair>(0) : affairs;
	}

	public static void setWorkflowAssignedAllAffairs(List<CtpAffair> saveAffairs) {
		
	//	Thread t = Thread.currentThread();
		
		List<CtpAffair> list = workflowAssignedAllAffairs.get();
		if (list == null) {
			list = new ArrayList<CtpAffair>();
			workflowAssignedAllAffairs.set(list);
		}
		/*System.out.println(t.getName()+"|"+t.getName() + "+++" + t.toString() + "--------" + list.size()+"---------hash:"+list.hashCode()+" ---toString:"+list.toString());*/
		list.clear();
		list.addAll(saveAffairs);
	}
	
	public static Long[] getCurrentUserData() {
		return currentUserData.get();
	}
	public static void setCurrentUserData(Long[] currentUser) {
		currentUserData.set(currentUser);
	}
	public static List<Long[]> getPushMessageMembers(){
    	List<Long[]> list = pushMessageMembers.get();
    	return  list== null ? new ArrayList<Long[]>(0) : list ;
    }
    public static void setPushMessageMembers(List<Long[]> l){
    	List<Long[]> list = pushMessageMembers.get();
    	if(list == null){
    		list = new ArrayList<Long[]>();
    		pushMessageMembers.set(list);
    	}
    	list.addAll(l);
    }
    public static Map<String,Object> getTraceDataMap() {
    	Map<String,Object> map = traceDataMap.get();
    	return map == null ? new HashMap<String,Object>(0) : map;
	}
    public static void addToTraceDataMap(String s,Object obj) {
    	Map<String,Object> map = traceDataMap.get();
    	if(null == map){
    		map = new HashMap<String,Object>();
    		traceDataMap.set(map);
    	}
    	map.put(s,obj);
	}
    
	public static Map<String, String> getRepeatAffairs() {
		Map<String, String> map = repeatAffairs.get();
		return map == null ? new HashMap<String, String>(0) : map;
	}

	public static void addRepeatAffairs(String s, String obj) {
		Map<String, String> map = repeatAffairs.get();
		if (null == map) {
			map = new HashMap<String, String>();
			repeatAffairs.set(map);
		}
		map.put(s, obj);
	}
	public static Integer getOperationType() {
		if (null == dateSharedWithWorkflowEngine.get()) {
			setOperationType(12);
		}
		return (Integer) dateSharedWithWorkflowEngine.get();
	}

	public static void setOperationType(Integer obj) {
		dateSharedWithWorkflowEngine.set(obj);
	}
	
	/**
	 * 
	 * @param opinionId 意见Id，用于锚点
	 * @param isHidden 是否隐藏
	 * @param opinion 意见内容
	 * @param attitude 意见态度
	 */
	public static void setFinishWorkitemOpinionId(long opinionId, Boolean isHidden, String opinion, int attitude, boolean isUploadAtt){
		finishWorkitemOpinion.set(new Object[]{opinionId, isHidden, opinion, attitude, isUploadAtt});
	}
	
	
	public static Map<String, String> getRepalAtt() {
		Map<String, String> map = repealAtt.get();
		return map == null ? new HashMap<String, String>(0) : map;
	}

	public static void addRepalAtt(String s, String obj) {
		Map<String, String> map = repealAtt.get();
		if (null == map) {
			map = new HashMap<String, String>();
			repealAtt.set(map);
		}
		map.put(s, obj);
	}
	
	public static Long getFinishWorkitemOpinionId(){
		Object[] o = finishWorkitemOpinion.get();
		return o == null ? null : (Long)o[0];
	}
	public static boolean getFinishWorkitemOpinionHidden(){
		Object[] o = finishWorkitemOpinion.get();
		return o == null ? false : Strings.isTrue((Boolean)o[1]);
	}
	public static String getFinishWorkitemOpinion(){
		Object[] o = finishWorkitemOpinion.get();
		return o == null ? "" : (String)o[2];
	}
	public static int getFinishWorkitemOpinionAttitude(){
		Object[] o = finishWorkitemOpinion.get();
		return o == null ? -1 : (Integer)o[3];
	}
	public static boolean getFinishWorkitemOpinionUploadAtt(){
		Object[] o = finishWorkitemOpinion.get();
		return o == null ? false : (Boolean)o[4];
	}
	
	public static Object getColSummary() {
		return colSummaryTL.get();
	}

	public static void setColSummary(Object colSummary) {
		colSummaryTL.set(colSummary);
	}
		
	/**
	 * 设置不入库
	 */
	public static void setNoIndex(){
		isNeedIndex.set(false);
	}
	
	/**
	 * 是否需要入库
	 * 
	 * @return
	 */
	public static boolean isNeedIndex(){
		Boolean i = isNeedIndex.get();
		if(i == null){
			return true;
		}
		
		return i.booleanValue();
	}

	/**
	 * 设置不入库
	 */
	public static void setProcessFinished(){
		isProcessFinished.set(true);
	}
	
	/**
	 * 是否需要入库
	 * 
	 * @return
	 */
	public static boolean isProcessFinished(){
		Boolean i = isProcessFinished.get();
		if(i == null){
			return false;
		}
		
		return i.booleanValue();
	}
	
	/**
	 * 设置不入库
	 */
	public static void setIsNeedAutoSkip(boolean b){
		isNeedAutoSkip.set(b);
	}
	
	/**
	 * 是否需要入库
	 * 
	 * @return
	 */
	public static boolean isNeedAutoSkip(){
		Boolean i = isNeedAutoSkip.get();
		if(i == null){
			return true;
		}
		
		return i.booleanValue();
	}

	public static void setTheStopAffair(CtpAffair a){
	    theStopAffairTL.set(a);
	}
    
    public static CtpAffair getTheStopAffair(){
        return (CtpAffair)theStopAffairTL.get();
    }
    
    public static void addToAllStepBackAffectAffairMap(long memberId, long affairId){
    	Map<Long, Long> map = allStepBackAffectAffairMap.get();
    	if(map == null){
    		map = new HashMap<Long, Long>();
    		allStepBackAffectAffairMap.set(map);
    	}
    	
    	map.put(memberId, affairId);
    }
    /**
     * 存储memberId和activityId对应的affair对象，用来更新signet_htm_document对应的记录的affairId
     * @param maId
     * @param affair
     */
    public static void addToMemberIdAndActivityId2AffairMap(String maId, CtpAffair affair){
    	Map<String, CtpAffair> map = memberIdAndActivityId2AffairMap.get();
    	if(map == null){
    		map = new HashMap<String, CtpAffair>();
    		memberIdAndActivityId2AffairMap.set(map);
    	}
    	
    	map.put(maId, affair);
    }
    
    public static Map<String, CtpAffair> getAllMemberIdAndActivityId2AffairMap(){
    	Map<String, CtpAffair> map = memberIdAndActivityId2AffairMap.get();
    	
    	return map == null ? new HashMap<String, CtpAffair>(0) : map;
    }
    
    public static Map<Long, Long> getAllStepBackAffectAffairMap(){
    	Map<Long, Long> map = allStepBackAffectAffairMap.get();
    	
    	return map == null ? new HashMap<Long, Long>(0) : map;
    }
    public static void addToAllSepcialStepBackCanceledAffairMap(Long memberId, Long affairId[]){
        Map<Long, Long[]> map = allSepcialStepBackCanceledAffairMap.get();
        if(map == null){
            map = new HashMap<Long, Long[]>();
            allSepcialStepBackCanceledAffairMap.set(map);
        }
        
        map.put(memberId, affairId);
    }
    
    public static Map<Long, Long[]> getAllSepcialStepBackCanceledAffairMap(){
        Map<Long, Long[]> map = allSepcialStepBackCanceledAffairMap.get();
        
        return map == null ? new HashMap<Long, Long[]>(0) : map;
    }
    
    public static void addToAffairMap(long memberId, long affairId){
    	Map<Long, Long> map = affairMap.get();
    	if(map == null){
    		map = new HashMap<Long, Long>();
    		affairMap.set(map);
    	}
    	
    	map.put(memberId, affairId);
    }
    
    public static Map<Long, Long> getAffairMap(){
    	Map<Long, Long> map = affairMap.get();
    	
    	return map == null ? new HashMap<Long, Long>(0) : map;
    }
    
    public static void setFinishAffairId(Long affairId){
    	finishAffairId.set(affairId);
    }
    
    public static Long getFinishAffairId(){
    	return finishAffairId.get();
    }
    
    public static Map<Long, Long> getAffairDistributeMap() {
    	Map<Long, Long> map = affairDistributeMap.get();
    	
    	return map == null ? new HashMap<Long, Long>(0) : map;	
	}

	public static void addAffairDistributeMap(Long memberId,Long affairId) {
		Map<Long, Long> map = affairDistributeMap.get();
    	if(map == null){
    		map = new HashMap<Long, Long>();
    		affairDistributeMap.set(map);
    	}
    	
    	map.put(memberId, affairId);
	}

	public static void removeWorkflowAssignedAllAffairs(){
    	workflowAssignedAllAffairs.remove();
    }
    
	public static void remove() {
		finishWorkitemOpinion.remove();
		dateSharedWithWorkflowEngine.remove();
		colSummaryTL.remove();
		isNeedIndex.remove();
		theStopAffairTL.remove();
		affairMap.remove();
		finishAffairId.remove();
		pushMessageMembers.remove();
		currentUserData.remove();
		allStepBackAffectAffairMap.remove();
		allSepcialStepBackCanceledAffairMap.remove();
		memberIdAndActivityId2AffairMap.remove();
		traceDataMap.remove();
		isNeedAutoSkip.remove();
		isProcessFinished.remove();
		repeatAffairs.remove();
		repealAtt.remove();
		isProcessCompetion.remove();
		affairDistributeMap.remove();
		removeWorkflowAssignedAllAffairs();
	}
}
