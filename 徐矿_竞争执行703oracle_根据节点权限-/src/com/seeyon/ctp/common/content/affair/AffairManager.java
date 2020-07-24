/**
 * $Author: muj $
 * $Rev: 15977 $
 * $Date:: 2015-01-30 11:21:26#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.common.content.affair;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.collaboration.bo.WorkflowAnalysisParam;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.FlipInfo;

/**
 * <p>
 * Title: T1开发框架
 * </p>
 * <p>
 * Description: 内容组件封装Affair事项处理接口
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 * 
 * @since CTP2.0
 */
public interface AffairManager {
	/**
	 * 保存事项
	 * @param affair
	 */
	public void save(CtpAffair affair) throws BusinessException;

	/**
	 * 保存事项列表
	 * @param affairs 事项列表
	 * @throws BusinessException
	 */
	public void saveAffairs(List<CtpAffair> affairs) throws BusinessException;

	/**
	 * 根据id查询affair事项记录
	 * @param id
	 * @return CtpAffair
	 * @throws BusinessException
	 */
	public CtpAffair get(Long id) throws BusinessException;

	/**
	 * 获取发起人事项（已发事项）
	 * @param summaryId Summary表id
	 * @return CtpAffair
	 */
	public CtpAffair getSenderAffair(Long summaryId) throws BusinessException;

	/**
	 * 逻辑删除-根据应用类别枚举和子应用Id删除记录
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param subObjectId 子应用Id
	 * @return
	 */
	public void deleteBySubObject(ApplicationCategoryEnum appEnum,Long subObjectId) throws BusinessException;

	/**
	 * 根据应用类别枚举和objectId删除记录（逻辑删除）
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param objectId summary表id
	 * @return
	 */
	public void deleteByObjectId(ApplicationCategoryEnum appEnum,Long objectId) throws BusinessException;

	/**
	 * 根据主记录Id和人员Id删除事项（逻辑删除）
	 * @param objectId summary表id
	 * @param memberId 事项记录所属人对应的人员id
	 * @return
	 */
	public void deleteAffair(Long objectId, Long memberId) throws BusinessException;

	/**
	 * 根据ID删除事务（逻辑删除）
	 * @param id CtpAffair的Id
	 * @return
	 */
	public void deleteAffair(Long id) throws BusinessException;
	
	/**
	 * 物理删除 - 根据ID删除事务
	 * @param id 事项Id
	 * @throws BusinessException
	 */
	public void deletePhysical(Long id)throws BusinessException;
	/**
	 * 物理删除 - 根据ObjectId删除事项
	 * @param objectId 主应用Id,如协同(col_summary)的Id
	 * @throws BusinessException
	 */
	public void deletePhysicalByObjectId(Long objectId)throws BusinessException ;
	/**
	 * 物理删除 - 根据Object 和 memberId删除事项 
	 * @param objectId 主应用Id,如协同(col_summary)的Id
	 * @param memberId 人员Id
	 * @throws BusinessException
	 */
	public void deletePhysical(Long objectId, Long memberId)throws BusinessException ;
	/**
	 * 物理删除 - 根据应用和Object 和 memberId删除事项 
	 * @param app 应用类别枚举ApplicationCategoryEnum
	 * @param objectId 主应用Id,如协同(col_summary)的Id
	 * @param memberId 人员Id
	 * @throws BusinessException
	 */
	public void deletePhysical(ApplicationCategoryEnum app, Long objectId, Long memberId)throws BusinessException;
	
	/**
	 * 更新affair对象
	 * @param affair CtpAffair对象
	 * @return
	 */
	public void updateAffair(CtpAffair affair) throws BusinessException;
	/**
	 * 批量更新affair事项
	 * @param affairs affair事项列表
	 * @throws BusinessException
	 */
    public void updateAffairs(List<CtpAffair> affairs) throws BusinessException ;
	

	/**
	 * 更新行，指定字段
	 * @param affairId 事项记录id
	 * @param columnValue key-字段名， value -字段值
	 */
	public void update(Long affairId, Map<String, Object> columnValue) throws BusinessException;
	

	/**
	 * 更新行，指定字段
	 * @param affairId 事项记录id
	 * @param columnValue key-字段名， value -字段值
	 */
	public void update(String hql, Map<String, Object> params) throws BusinessException;

	/**
	 * 根据id更新指定列的数据，不再需要先get然后update 使用该方法，必须使用泛型
	 * 
	 * @param columns
	 *            key - 列名 value - 值，注意：值的类型必须和数据类型一致，否则异常，value中允许有
	 *            <code>null</code>，否则请用
	 *            {@link #update(Class, String[], Object[], Type[], Object[][])}
	 * @param wheres
	 *            更新条件,多行2列数组，第一列是列名(String)，第二列是值(Object)，可以为<code>null</code>
	 *            ,如：<code>new Object[][]{{"name", "tanmf"}, {"age", 21}}</code>
	 */
	public void update(Map<String, Object> columns, Object[][] wheres)throws BusinessException;

	/**
	 * 根据特定条件查询
	 * @param  flipInfo 分页对象，不分页的情况就传null
	 * @param  conditions 条件Map键值对  key - 数据库列名    value 数据库的value值， <br>
	 *                    value值支持直接传递一个list或者具体值,如果value值是list,后台会采用in的方式来进行查询
	 * @return List<CtpAffair>
	 */
	public List<CtpAffair> getByConditions(FlipInfo flipInfo,Map conditions)throws BusinessException;

	/**
	 * 根据特定条件查询数量
	 * 
	 * @param flipInfo
	 * @param conditions
	 * @return
	 * @throws BusinessException
	 *
	 * @Author      : xuqw
	 * @Date        : 2016年6月24日上午10:58:22
	 *
	 */
	public int getCountByConditions(Map conditions)throws BusinessException;
	
	public int getCountByConditionsHis(Map conditions)throws BusinessException;

	/**
	 * 根据应用和主运用ID查找
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param objectId summary表id
	 * @return List<CtpAffair>
	 */
	public List<CtpAffair> getAffairs(ApplicationCategoryEnum appEnum, Long objectId) throws BusinessException;

	/**
	 * 根据应用和主运用ID查找affairs，查找历史表
	 * @param appEnum  应用类别枚举ApplicationCategoryEnum  
	 * @param objectId
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairsHis(ApplicationCategoryEnum appEnum, Long objectId)throws BusinessException;
	
	/**
	 * 获取人员的Affair列表， 参见{@link #getAffairs(ApplicationCategoryEnum, Long, Long)}
	 * 
	 * @param appEnum 应用类型
	 * @param objectId 业务关联ID
	 * @param membeId 人员ID
	 * @return
	 * @throws BusinessException
	 *
	 * @Since A8-V5 6.1
	 * @Author      : xuqw
	 * @Date        : 2017年2月15日下午3:05:52
	 *
	 */
	public List<CtpAffair> getAffairsHis(ApplicationCategoryEnum appEnum, Long objectId, Long membeId)throws BusinessException;
	
	/**
	 * 根据主运用ID查找
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param objectId summary表id
	 * @return List<CtpAffair>
	 */
	public List<CtpAffair> getAffairs(long objectId) throws BusinessException;
	
	
	
	/**
	 * 通过SummaryId得到用户的所有Affair
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param objectId Summary表id
	 * @param memeberId 事项记录所属人的人员id
	 * @return List<CtpAffair>
	 */
	public List<CtpAffair> getAffairs(ApplicationCategoryEnum appEnum, Long objectId, Long memeberId) throws BusinessException;

	/**
     * 通过App,ObjectId,SubObjectId,MemberId得到用户的所有Affair
     * @param appEnum 应用类别枚举ApplicationCategoryEnum
     * @param objectId Summary表id
     * @param subObjectId 子应用Id
     * @param memeberId  事项记录所属人的人员id
     * @return List<CtpAffair>
     */
    public List<CtpAffair> getAffairs(ApplicationCategoryEnum appEnum, Long objectId, Long subObjectId,Long memberId) throws BusinessException;
    	
	/**
	 * 根据协同id和事项状态获取事项列表
	 * @param objectId summary表中的id
	 * @param state 状态枚举StateEnum
	 * @return List<CtpAffair>
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairs(Long objectId,StateEnum state) throws BusinessException;
	
	/**
	 * 根据协同id和事项状态获取事项列表
	 * @param objectId summary表中的id
	 * @param state 状态枚举StateEnum
	 * @return List<CtpAffair>
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairs(Long objectId,StateEnum state,SubStateEnum subState) throws BusinessException;
	
	/**
	 * 根据协同id和事项状态获取事项列表
	 * @param objectId summary表中的id
	 * @param state 状态枚举StateEnum
	 * @return List<CtpAffair>
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairs(Long objectId,List<StateEnum> states) throws BusinessException;
	
	
	/**
	 * 根据应用类型、summaryId和activityId获取事项列表
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param objectId summary表id
	 * @param activityId  流程实例id(activityId)
	 * @return List<CtpAffair>
	 */
	public List<CtpAffair> getAffairsByObjectIdAndNodeId(Long objectId, Long activityId)throws BusinessException;

	/**
	 * 根据节点Id(同一个模板所有的流程的节点ID是相同的)来获取事项列表
	 * @param activityId 节点id,affair表的activityId
	 * @return
	 */
	public List<CtpAffair> getAffairsByNodeId(Long activityId);
	
	/**
	 * 通过subObjectId得到一个affair
	 * @param subObjectId 子应用id
	 * @return CtpAffair
	 */
	public CtpAffair getAffairBySubObjectId(Long subObjectId) throws BusinessException;

	/**
	 * 通过objectId得到有效的affair列表, 只用于协同
	 * 有效事项：state不等于(col_cancel、col_stepBack、col_takeBack、col_competeOver)的
	 * 
	 * @param appEnum 应用类别枚举ApplicationCategoryEnum
	 * @param objectId summary表id
	 * @return List<CtpAffair>
	 */
	public List<CtpAffair> getValidAffairs(ApplicationCategoryEnum appEnum, Long objectId) throws BusinessException;

	/**
	 * 查询跟踪指定协同的所有affair
	 * @param objectId summary表id
	 * @return List<CtpAffair> 仅取以下字段：id,senderId,memberId,state,track,forwardMember,transactorId,delete
	 */
	public List<CtpAffair> getValidTrackAffairs(Long objectId) throws BusinessException;

	/**
	 * 通过人员id，找出该人员最早未处理的待办时间
	 * @param memberId 事项记录所属人的人员id
	 * @return Date
	 */
	public Date getMinStartTimePending(Long memberId) throws BusinessException;
    
    /**
     * 查找待办或者设置了跟踪的已办事项
     * @param objectId ： 主应用id
     * @param category 应用类别枚举{@link com.seeyon.ctp.common.constants.ApplicationCategoryEnum}的key值
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getTrackAndPendingAffairs(Long objectId, Integer category) throws BusinessException;
    
    /**
     * 取得与表单业务配置相关的表单模板相关的跟踪事项记录总数
     * @param memberId	当前用户ID
     * @param tempIds	业务配置对应的表单模板ID集合
     */
    public int getTrackCount4BizConfig(Long memberId, List<Long> tempIds);
   	
    /**
	 * 将所有事项更新成撤销状态，例如撤销流程的时候，需要将所有事项更新成撤销状态。<br>
     * 推荐使用： {@link #update(Class, String[], Object[], Type[], Object[][])}
     * @param objectId 主应用Id
	 */
	public void updateAffairsState2Cancel(Long objectId) throws BusinessException;
	
	
    /**
     *  更新事项的状态和子状态
	 * @param stateEnum 状态枚举,将事项更新成为传入的这个值
	 * @param subStateEnum 子状态枚举，将事项更新成为传入的这个值
	 * @param objectId 协同id，更新事项的where条件之一
     * @param subObjectIds 子应用id列表，更新事项的where条件之一
     * @throws BusinessException
     */
	public void updateByObjectIdAndSubObjIds(StateEnum stateEnum,SubStateEnum subStateEnum, Long objectId,List<Long> subObjectIds) throws BusinessException;
	
	/**
	 * 批量修改个人事项的完成状态，将affair的finish更新为true并且设置成不跟踪(track=no)
	 * @param objectId 主应用Id
	 */
	public void updateFinishFlag(Long objectId) throws BusinessException;
	
    
    /**
     * 查询某协同下的所有有效事项，不包含待发数据
     * 分为分页和不分页两种，如果不分页则 ，则在传参时flipInfo置为null即可
     * @param flipInfo 如果该参数为null 则查询不分页 
     * @param params 查询条件，可用的查询条件：
     * 		    objectId,必须包含 objectId(协同id)<br>
     *  		app 应用类别枚举ApplicationCategoryEnum的key值<br>
     *  		subState 子状态<br>
     *  		delete 是否删除<br>
     * @return
     * @throws BusinessException
     */
	public List<CtpAffair> getValidAffairs(FlipInfo flipInfo,Map params) throws BusinessException;
	
	/**
     * 提供给F111专用接口，查询某个人流程相关的数据（协同/公文的已发已办，待办数据）
     * @param flipInfo 分页对象
     * @param params   需要设置的参数 memberId、senderId
     * @return
     */
    public List<CtpAffair> getWorkflowRelatedAffairs(FlipInfo flipInfo, Map params) throws BusinessException;
   
    /**
     * 提供给F111接口，查询某个人的所有的会议的数据
     * @param flipInfo 分页对象
     * @param params 需要设置的参数 memberId、senderId
     * @return List<CtpAffair>
     * @throws BusinessException
     */
    public List<CtpAffair> getSenderOrMemberMtList(FlipInfo flipInfo, Map params) throws BusinessException;
   
    /**
     * 流程分析
     * @param WorkflowAnalysisParam 参数对象{@link com.seeyon.ctp.common.content.affair.WorkflowAnalysisParam}}
     * @return
     */
    public Map<Long,Integer>  getOverNodeCount(WorkflowAnalysisParam param) throws BusinessException;
    
	/**
	 * 流程分析
	 * @param WorkflowAnalysisParam 参数对象{@link com.seeyon.ctp.common.content.affair.WorkflowAnalysisParam}}
	 * @return
	 */
    public Map<Long,String>  getNodeCountAndSumRunTime(WorkflowAnalysisParam param) throws BusinessException;
	/**
	 * 获取事项 - 流程分析 - 节点分析
	 * @param WorkflowAnalysisParam 参数对象{@link com.seeyon.ctp.common.content.affair.WorkflowAnalysisParam}}
	 * @return
	 */
    public List<CtpAffair> getAffairByActivityId(WorkflowAnalysisParam param) throws BusinessException;
     /**
      * 流程分析 - 综合分析
      * @param WorkflowAnalysisParam 参数对象{@link com.seeyon.ctp.common.content.affair.WorkflowAnalysisParam}}
      * @return
      */
	 public Map<Long,String> getStaticsByActivityId(WorkflowAnalysisParam param) throws BusinessException;
	 /**
	  * 流程分析，超时分析
	  * @param WorkflowAnalysisParam 参数对象{@link com.seeyon.ctp.common.content.affair.WorkflowAnalysisParam}}
	  * @return
	  */
	 public Map<Long,Integer> getOverCountByMember(WorkflowAnalysisParam param) throws BusinessException;
	
	 /**
	  * 设置已发事项的归档信息，即将archiveId设置进对应字段(用与预归档之后的正式归档)
	  * @param objectId 协同Id
	  * @param archiveId 归档Id
	  */
	 public void updateSentPigeonholeInfo(Long objectId, Long archiveId) throws BusinessException;
	 
	 /**
	  * 设置当前协同所有事项的归档信息
	  * @param summaryId 主应用Id
	  * @param archiveId 归档文件夹Id
	  */
	 public void updateAllPigeonholeInfo(Long summaryId, Long archiveId) throws BusinessException;
	 
	 /**
	  * 获取流程节点所有的人员Id列表
	  * @param app 应用类型
	  * @param objectId  主应用Id
	  * @return
	  */
	 public List<Long> getAffairMemberIds(ApplicationCategoryEnum app, Long objectId)  throws BusinessException;
	 
	 /**
      * 分页查询Object对应的人员ID，滤重
      * @Author      : xuqw
      * @Date        : 2015年12月2日上午10:20:58
      * @param category 分类
      * @param objectId objectId
      * @param states 状态列表
      * @param flp 分页对象，flp==null时不分页
      * @return
      * @throws BusinessException
      */
     public List<Long> findMembers(ApplicationCategoryEnum category, Long objectId,
            List<StateEnum> states, FlipInfo flp) throws BusinessException;
	 
	 /**
	  * 获取当前节点的待办事项
	  * @param summaryId 主应用Id
	  * @param nodeIds 节点Id
	  * @return
	  * @throws BusinessException
	  */
	 public List<CtpAffair> getPendingAffairs(Long objectId, List<Long> nodeIds) throws BusinessException;

	 /**
	 * 判断指定的人员是否在某一个流程中，如果存在Affair则返回true,否则返回false.
	 * @param app 应用类别枚举ApplicationCategoryEnum
	 * @param objectId 主应用Id
	 * @param memberIds 人员Id列表
	 * @return
	 */
	public boolean isAffairInProcess(ApplicationCategoryEnum app,Long objectId, List<Long> memberIds)  throws BusinessException;
	
    
    /**
     * 从历史表中获取Affair
     * @param objectId 主应用Id
     * @param activityId 节点Id
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getAffairsHis(Long objectId, Long activityId) throws BusinessException;
       
   
    /**
     * 从历史库中查找已发事项
     * @param summaryId
     * @return
     * @throws BusinessException
     */
    public CtpAffair getSenderAffairByHis(Long summaryId) throws BusinessException ;
    /**
     * 从历史库中通过ID来查找
     * @param id 事项Id
     * @return
     * @throws BusinessException
     */
    public CtpAffair getByHis(Long id) throws BusinessException ;
    /**
     * 从历史库中通过ID来查找
     * @param id
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getValidAffairsHis(ApplicationCategoryEnum appEnum, Long objectId)throws BusinessException ;
    /**
     * 从历史库中通过ID来查找
     * @param id
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getValidAffairsHis(FlipInfo flipInfo,Map<String,Object> params) throws BusinessException;
    

	 /**
	 * 更新所有可用的待办事项
	 * @param appEnum  应用类别枚举ApplicationCategoryEnum
	 * @param objectId 主应用Id
	 * @param values 需要更新的值，affair的所有字段都可以
    */
	public void updateAffairs(ApplicationCategoryEnum appEnum, Long objectId,Map<String, Object> values) throws BusinessException;
	
    
    /**
     * 根据affair得到错误提示消息，回退，撤销，取回等
     * @param affair
     * @return 如果事项可用的则返回"",如果返回不为空,则为不可用的提示语
     */
    public String getErrorMsgByAffair(CtpAffair affair) throws BusinessException;
    
    
    /**
	 * 获取某一个人的代理的待办事项数目之和
	 * @param memberId 人员id
	 * @return 
	 * @throws BusinessException
	 */
    public int getAgentPendingCount(Long memberId) throws BusinessException;
    
    /**
     * 将待办事项更新为已读状态
     * @param affair
     * @throws BusinessException
     */
    public void updateAffairReaded(CtpAffair affair) throws BusinessException ;
    
    
    /**
     * 验证affair是否是有效数据
     * @param affair 当前事项
     * @param isDeleteValid 删除事项是否有效 true:删除事项有效，返回值true, false:删除事项无效，返回值false
     * @return
     */
    public boolean isAffairValid(CtpAffair affair,Boolean isDeleteValid);
    
    public CtpAffair getSimpleAffair(Long id) throws BusinessException;
	public Object getAffairListBySender(Long memberId, String orgStr,AffairCondition condition,boolean onlyCount, FlipInfo fi,List<Integer> appEnum,String... groupByPropertyName);
	public Object getAffairListBySender(Long memberId, String orgStr,AffairCondition condition,boolean onlyCount, FlipInfo fi,List<Integer> appEnum,boolean isGroupBy,String... groupByPropertyName);
	
	/**
     * 更新第一次 关闭/处理 时间
     * @Author      : xuqw
     * @Date        : 2016年5月23日下午1:21:04
     * @param params
     *   params.affairId Long 待办事项ID
     *   params.accountId Long 待办事项发起单位ID
     *   params.firstViewTime Long 待办事项第一次查看时间，Date转换成long
     *   [params.signleViewPeriod] String 第一次 关闭/处理 时间, 这个时间为空的时候才进行更新， 不为空不做处理
     * @throws BusinessException
     */
	public void updateSignleViewTime(Map<String, String> viewRecordMap) throws BusinessException;
	public void updateSignleViewTimes(List<Map<String, String>> viewRecordMaps) throws BusinessException;

	/**
     * 更新第一次 关闭/处理 时间
     * @Author      : xuqw
     * @Date        : 2016年5月23日下午1:21:04
     * @param params
     *   params.affairId Long 待办事项ID
     *   params.accountId Long 待办事项发起单位ID
     *   params.firstViewTime Long 待办事项第一次查看时间，Date转换成long
     *   [params.signleViewPeriod] String 第一次 关闭/处理 时间, 这个时间为空的时候才进行更新， 不为空不做处理
     * @throws BusinessException
     */
    void updateSignleViewTime(CtpAffair affair) throws BusinessException;

    /**
     * 记录用户处理动作时间, 第一次操作， 第一次处理/关闭等时间
     * @Author      : xuqw
     * @Date        : 2016年5月23日下午4:01:16
     * @param affair
     * @throws BusinessException
     */
    void updateAffairAnalyzeData(CtpAffair affair) throws BusinessException;
    
    /**
     * 获取不等于某个节点权限的affair；只取id,senderId,memberId,state,nodePolicy字段
     * @param flipInfo
     * @param objectId
     * @param policy
     * @param states
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getAffairsByObjectIdAndStates(FlipInfo flipInfo,Long objectId,List<Integer> states) throws BusinessException;
    
    
    /**
     * 同一流程只显示一条
     * @param condition
     * @param onlyCount
     * @param fi
     * @param appEnum
     * @return
     * @throws BusinessException
     */
    public Object getDeduplicationAffairs(Long memberId,AffairCondition condition,boolean onlyCount, FlipInfo fi) throws BusinessException;
    
    /**
     * 更新表单协同的标题
     * @param summaryId 协同主表ID
     * @param newSubject 新标题
     */
	public void updateFormCollSubject(Long summaryId, String newSubject) throws BusinessException ;
	
	/**
	 * 根据应用类型和objectId查询所有事项的id
	 * @param appEnum 枚举
	 * @param objectId 
	 * @return
	 * @throws BusinessException
	 */
	public List<Long> getAllAffairIdByAppAndObjectId(ApplicationCategoryEnum appEnum,Long objectId) throws BusinessException;
    
	/**
	 * 分组查询事项状态
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List getAffairDetailsBygorup(Map<String,Object> params) throws BusinessException ;
	
	public Integer getStartAffairStateByObjectId(Long objectId) throws BusinessException ;
	
	/**
	 * 更新事项冗余STATE字段
	 * @param objectId
	 * @param summaryState   summary表中的state字段
	 * @throws BusinessException
	 */
	public void updateAffairSummaryState(Long objectId, Integer summaryState) throws BusinessException;
	/**
	 * 查询当前待办人的affair，非知会
	 */
	public List<CtpAffair> getAffairsForCurrentUsers(FlipInfo flipInfo, Map<String,Object> map) throws BusinessException;
	
	/**
	 * 当前待办的数量
	 * @param memberId
	 * @return
	 * Map
	 * key : ApplicationCategoryEnum.key 
	 * value : 数值
	 * @throws BusinessException 
	 */
	public Map<String, Integer> countPendingAffairs(long memberId,String[] appKeys) throws BusinessException;
	
	/**
	 * 组装待办事项中代理的查询条件
	 * @param condition
	 * @param parameter
	 * @return
	 */
	public StringBuilder getCondition4Agent(AffairCondition condition, Map<String,Object> parameter, boolean isHQL);
	
	/**
	 *  查询指定时间段内，智能处理的记录，按处理人分组
	 * @param endTime 开始时间
	 * @param beginTime 结束时间
	 * @return
	 */
	public List getAIProcessingCountByMemberId(Date beginTime, Date endTime);
	
	/**
	 *  根据当前待办人，正文类型，state状态查询
	 * @param memberId
	 * @param bodyTypeList
	 * @param state
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairListByMemberIdBodyTypeAndState(Long memberId,List<String> bodyTypeList,StateEnum state)
			throws BusinessException;
	
	/**
	 * 获取时间段内流程期限到期的待办
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getProcessOverdueAffairs(Date beginTime,Date endTime) throws BusinessException;
	
	/**
	 *  获取时间段内节点期限到期的待办
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getNodeOverdueAffairs(Date beginTime,Date endTime) throws BusinessException;
	
	/**
	 * 获取时间段内要召开的会议待办
	 * @param beginTime 开始时间
	 * @param endTime 结束时间
	 * @param appEnum 
	 * @param stateEnum
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairsByAppAndReceivetimeAndState(ApplicationCategoryEnum appEnum,Date beginTime, Date endTime,StateEnum stateEnum) throws BusinessException;

	/**
	 *  更新待办事项权重值
	 * @param sortWeight
	 * @param affairIdList
	 */
	public void updateSortWeight(int sortWeight, List<Long> affairIdList) throws BusinessException;
	
	/**
	 * 是否是加签节点产生的待办事项
	 * 目前判断了：加签\只会\当前会签\多级会签\传阅  5种情况
	 * @param affair
	 * @return
	 */
	public boolean isAddNodeAffair(CtpAffair affair);
	
	/**
	 * 根据节点权限获取affairs
	 * add by shenwei
	 * 20200724
	 * @param a
	 * @return
	 * @throws BusinessException 
	 */
	public List<CtpAffair> getAffairsByNodePolicy(String a) throws BusinessException;
}
