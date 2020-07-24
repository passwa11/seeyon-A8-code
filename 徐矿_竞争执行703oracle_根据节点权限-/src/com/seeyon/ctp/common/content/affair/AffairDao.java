/**
 * $Author: muj $
 * $Rev: 16092 $
 * $Date:: 2015-02-06 17:49:48#$:
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

import org.hibernate.criterion.DetachedCriteria;

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
 * Description: 内容组件封装Affair数据处理接口
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
public interface AffairDao {
	/**
	 * 保存
	 * 
	 * @param affair
	 */
	public void save(CtpAffair affair) throws BusinessException;

	public void saveAffairs(List<CtpAffair> affairs) throws BusinessException;

	/**
	 * 超找
	 * 
	 * @param id
	 */
	public CtpAffair get(Long id);
	
	public CtpAffair getByHis(Long id) throws BusinessException;

	/**
	 * 查找
	 * 
	 * @param affair
	 */
	public void update(CtpAffair affair) throws BusinessException;
	
	public void update(String hql, Map<String, Object> params) throws BusinessException;
	
	public void updateAffairs(List<CtpAffair> affairs) throws BusinessException;
	/**
	   * 通过activity条件更新
	   */
	public void updateAffairByActivity(Long objectId,List<Long> activities,Integer state,Integer subState,Integer otherwise) throws BusinessException;

	/**
	 * 根据主应用ID查找
	 * 
	 * @param app
	 * @param objectId
	 * @return
	 */
	public CtpAffair getByObjectId(ApplicationCategoryEnum app, Long objectId)
			throws BusinessException;

	/**
	 * 获取发起人事项
	 * 
	 * @param summaryId
	 * @return
	 */
	public CtpAffair getSenderAffair(Long summaryId) throws BusinessException;

	public CtpAffair getSenderAffairHis(Long objectId) throws BusinessException ;
	
	public Integer getStartAffairStateByObjectId(Long objectId) throws BusinessException;
	
	/**
	 * 根据应用和主应用ID删除记录（逻辑删除）
	 * 
	 * @param appEnum
	 * @param objectId
	 */
	public void deleteByAppAndObjectId(ApplicationCategoryEnum appEnum,
			Long
			
			objectId) throws BusinessException;

	/**
	 * 根据主题Id和人员Id删除事项（逻辑删除）
	 * 
	 * @param objectId
	 * @param memberId
	 */
	public void deleteByObjectIdAndMemberId(Long objectId, Long memberId)
			throws BusinessException;

	/**
	 * 根据特定条件查询，结果不支持分页
	 * 
	 * @param conditions
	 * @return
	 */
	public List<CtpAffair> getByConditions(FlipInfo flipInfo,Map conditions) throws BusinessException;
	
	/**
	 * 
	 * 根据特定条件查询数量
	 * 
	 * @param flipInfo
	 * @param conditions
	 * @return
	 * @throws BusinessException
	 *
	 * @Author      : xuqw
	 * @Date        : 2016年6月24日上午10:56:32
	 *
	 */
	public int getCountByConditions(Map conditions) throws BusinessException;


	public List<CtpAffair> getAffairsAll() throws BusinessException;

	/**
	 * 根据运用和住运用ID查找
	 * 
	 * @param collaboration
	 * @param summaryId
	 * @return
	 */
	public List<CtpAffair> getAffairsByAppAndObjectId(
			ApplicationCategoryEnum collaboration, long summaryId)
			throws BusinessException;

	public List<CtpAffair> getAffairsByAppAndObjectIdHis(
      ApplicationCategoryEnum collaboration, long summaryId)
      throws BusinessException;
	
	/**
	 * 根据ID删除事务
	 * 
	 * @param id
	 */
	public void delete(Long id) throws BusinessException;
	public void deletePhysicalById(Long id)throws BusinessException;
	public void deletePhysicalByObjectId(Long objectId)throws BusinessException ;
	public void deletePhysicalByObjectIdAndMemberId(Long objectId, Long memberId)throws BusinessException ;
	public void deletePhysicalByAppAndObjectIdAndMemberId(ApplicationCategoryEnum app, Long objectId, Long memberId)throws BusinessException;
	/**
	 * 查找待发协同的待发事项
	 * 
	 * @param app
	 * @param summaryId
	 * @param state
	 * @return
	 */
	public CtpAffair getWaitSendAffairByObjectIdAndState(
			ApplicationCategoryEnum collaboration, long summaryId, int key)
			throws BusinessException;

	/**
	 * 查询跟踪指定协同的所有affair
	 * 
	 * @param objectId
	 * @return 仅取以下字段：id,senderId,memberId,state,track,forwardMember,transactorId,delete
	 */
	public List<CtpAffair> getAvailabilityTrackingAffairsBySummaryId(
			Long objectId) throws BusinessException;

	/**
	 * 由流程实例id得到协同发起者的发起事项
	 * 
	 * @param subObjectId
	 * @return
	 */
	public List<CtpAffair> getSenderAffairsBySubObjectId(Long subObjectId)
			throws BusinessException;

	/**
	 * 通过objectId得到有效的affair列表,
	 * 
	 * @param name
	 * @param objectId
	 * @return
	 */
	public List<CtpAffair> getAvailabilityAffairsByAppAndObjectId(
			ApplicationCategoryEnum appEnum, Long objectId)
			throws BusinessException;

	
	/**
	 * 同{@link #getAvailabilityAffairsByAppAndObjectId(ApplicationCategoryEnum, Long)}, 获取分库表的数据
	 * 
	 * @param appEnum
	 * @param objectId
	 * @return
	 * @throws BusinessException
	 *
	 * @Since A8-V5 6.1
	 * @Author      : xuqw
	 * @Date        : 2017年2月15日下午3:26:12
	 *
	 */
	public List<CtpAffair> getAvailabilityAffairsByAppAndObjectIdHis(
            ApplicationCategoryEnum appEnum, Long objectId)
            throws BusinessException;
	
	/**
	 * 通过subObjectId得到一个affair
	 * 
	 * @param subObjectId
	 * @return
	 */
	public CtpAffair getAffairBySubObjectId(Long subObjectId)
			throws BusinessException;

	/**
	 * 根据应用类别、summaryId和用户id来查询affair列表
	 * 
	 * @param appEnum
	 * @param objectId
	 * @param userId
	 * @return
	 */
	public List<CtpAffair> getAffairsByObjectIdAndUserId(
			ApplicationCategoryEnum appEnum, Long objectId, Long userId)
			throws BusinessException;
	
	/**
	 * 
	 * {@link #getAffairsByObjectIdAndUserId(ApplicationCategoryEnum, Long, Long)}
	 * @param appEnum
	 * @param objectId
	 * @param userId
	 * @return
	 * @throws BusinessException
	 */
	public List<CtpAffair> getAffairsByObjectIdAndUserIdHis(
            ApplicationCategoryEnum appEnum, Long objectId, Long userId)
            throws BusinessException;
	
	  public List<CtpAffair> getAffairsByObjectIdAndSubObjectIdAndUserId(ApplicationCategoryEnum appEnum, Long objectId,
	            Long subObjectId, Long memberId) throws BusinessException ;
	/**
	 * 撤销流程更新事项状态和更新修改日期字段值
	 * 
	 * @param summaryId
	 * @param summaryState
	 * @return
	 */
	public void updateAffairsStateAndUpdateDate(Long summaryId)
			throws BusinessException;

	/**
	 * 根据应用类型、summaryId和activityId获取事项列表
	 * 
	 * @param appEnum
	 * @param objectId
	 * @param activityId
	 * @return
	 */
	public List<CtpAffair> getAffairsByObjectIdAndActivityId(Long objectId, Long activityId)
			throws BusinessException;

	/**
	 * 根据协同id和事项状态获取事项列表
	 * 
	 * @param objectId
	 * @param state
	 * @return
	 */
	public List<CtpAffair> getAffairsByObjectIdAndState(Long objectId,StateEnum state) throws BusinessException;
	public List<CtpAffair> getAffairsByObjectIdAndStates(Long summaryId,List<Integer> states) throws BusinessException;
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
	public void update(Map<String, Object> columns, Object[][] wheres)
			throws BusinessException;
	
	/**
	 * 由外部组装查询DetachedCriteria.然后在此执行。
	 * @param criteria
	 * @param flipInfo
	 * @return
	 */
    public List<CtpAffair> findPageCriteria(DetachedCriteria criteria,FlipInfo flipInfo) 
            throws BusinessException;
    /**
     * 由外部组装查询DetachedCriteria.然后在此执行。
     * @param criteria
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> findPageCriteria(DetachedCriteria criteria) 
            throws BusinessException;

    /**
     *  更新待办和已办事项的状态和子状态
	 * @param stateEnum 状态枚举
	 * @param subStateEnum 子状态枚举
	 * @param objectId 协同id
     * @param subObjectIds 子应用id
     * @throws BusinessException
     */
	public void updatePendingAndDoneAffairsByObjectIdAndSubObjectIds(
			StateEnum stateEnum, SubStateEnum subStateEnum, Long objectId,
			List<Long> subObjectIds);

	/**
     * 取得与表单业务配置相关的表单模板相关的跟踪事项记录总数
     * @param memberId	当前用户ID
     * @param tempIds	业务配置对应的表单模板ID集合
     */
	public int getTrackCount4BizConfig(Long memberId, List<Long> tempIds);
	/**
	 * 批量修改个人事项的完成状态
	 *
	 * @param objectId
	 * @param summaryState
	 */
	public void updateFinishFlag(Long objectId);


	public List<CtpAffair> getAffairsByActivityId(Long activityId);
	public List<CtpAffair> getPendingAffairListByObject(Long summaryId) throws BusinessException;
//	/**
//     * 取得与表单业务配置相关的表单模板相关的跟踪事项记录
//     * @param memberId	当前用户ID
//     * @param tempIds	业务配置对应的表单模板ID集合
//     */
//    public List<CtpAffair> getTrackList4BizConfig(Long memberId, List<Long> tempIds);
    /**
     * 查询某协同下的所有有效事项，不包含待发数据
     * 分为分页和不分页两种，如果不分页则 ，则在传参时flipInfo置为null即可
     * @param flipInfo 如果该参数为null 则查询不分页 
     * @param params 必须包含 objectId(协同id) ，
     *  app 如果不为空时，将添加该查询条件，否则不添加该参数查询
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getALLAvailabilityAffairList(FlipInfo flipInfo,Map params) throws BusinessException;
    
    /**
     * yangwulin 提供F111接口
     * @param flipInfo 分页对象
     * @param params  需要设置的参数 memberId、senderId
     * @return
     */
    public List<CtpAffair> getSenderOrMemberColAndEdocList(FlipInfo flipInfo, Map params) throws BusinessException;
    
    /**
     * 关联人员事项查询（发给/转给他人）
     * @param flipInfo 分页对象
     * @param params 需要设置的参数 memberId、senderId
     * @return
     * @throws BusinessException
     */
    public List<CtpAffair> getSenderColAndEdocList(FlipInfo flipInfo, Map params) throws BusinessException;
    
    /**
     * yangwulin  提供给F111接口
     * @param flipInfo 分页对象
     * @param params 需要设置的参数 memberId、senderId
     * @return List<CtpAffair>
     * @throws BusinessException
     */
    public List<CtpAffair> getSenderOrMemberMtList(FlipInfo flipInfo, Map params) throws BusinessException;
    
    public Map<Long,Integer>  getOverNodeCount(
            Long templeteId,
            Long accountId,
            boolean isCol,
            List<Integer> states,
            Date startDate,
            Date endDate);
    public Map<Long,String>  getNodeCountAndSumRunTime(
            Long templeteId,
            Long accountId,
            boolean isCol,
            List<Integer> states,
            Date startDate,
            Date endDate);
    public List<CtpAffair> getAffairByActivityId(
            Long templeteId,
            Long orgAccountId,
            boolean isCol,
            List<Integer> states,
            Long activityId,
            Date startDate,
            Date endDate);
   /**
    * 查找某个模板，指定节点指定时间段的所有事项。
    * @param templeteId
    * @param orgAccountId
    * @param isCol
    * @param states
    * @param activityId
    * @param startDate
    * @param endDate
    * @return
    */
    public Map<Long,String> getStaticsByActivityId(
            Long templeteId,
            Long orgAccountId,
            boolean isCol,
            List<Integer> states,
            Long activityId,
            Date startDate,
            Date endDate);
    /**
     * 根据应用和子应用ID删除记录（逻辑删除）
     * @param appEnum
     * @param subObjectId
     * @throws BusinessException
     */
    public void deleteByAppAndSubObjectId(ApplicationCategoryEnum appEnum,
            Long subObjectId) throws BusinessException;
    /**
     * 通过人员id，找出该人员最早未处理的待办时间
     * @param memberId
     * @return
     * @throws BusinessException
     */
    public Date getMinStartTimePending(Long memberId) throws BusinessException;

    public Map<Long,Integer> getOverCountByMember(
            Long templeteId,
            Long orgAccountId,
            boolean isCol,
            List<Integer> states,
            Long activityId,
            Date startDate,
            Date endDate);
    public List<Long> getMemberIdListByAppAndObjectId(ApplicationCategoryEnum app, Long id) ;
    public List<Long> getMemberIdListByAppAndObjectIdHis(ApplicationCategoryEnum app, Long id) ;
    
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
    
    public List<CtpAffair> getTrackingAndPendingAffairBySummaryId(Long summaryId,int app);
    public List<CtpAffair> getPendingAffairListByNodes(Long summaryId, List<Long> nodeIds) throws BusinessException ;
    /**
     * 首页PORTAL根据发起人查找
     * @param string
     * @param parameter 
     * @param fi
     * @return
     */
	public Object getAffairListBySender(final String sql, final Map<String, Object> parameter, final boolean onlyCount, final FlipInfo fi,final String... groupByPropertyName);
	/**
	 * 更新所有可用的待办事项
	 * @param appEnum
	 * @param objectId
	 * @param parameter
	 */
	public void updateAllAvailabilityAffair(ApplicationCategoryEnum appEnum,
			Long objectId, Map<String, Object> parameter);
	public boolean checkPermission4TheObject(ApplicationCategoryEnum app,
            Long objectId, List<Long> memberIds) ;
	/**
     * portal栏目指定发起人查询专用
     * @param PortalQueryParam  查询对象{@link com.seeyon.ctp.common.content.affair.PortalQueryParam}
     * @return
     */
	public Object getAffairListBySender(PortalQueryParam portalQueryParam) ;
	/**
     * 
     * @param objectId协同Id
     * @param activityId 节点ID
     * @return
     */
    public List<CtpAffair> getAffairsByActivityId(Long objectId,Long activityId) throws BusinessException;
    public  CtpAffair getEdocSenderAffair(Long objectId) throws BusinessException ;
    public int getCountAffairsByAppsAndStatesAndMemberId(List<ApplicationCategoryEnum> appEnums,List<StateEnum> statesEnums,Long memberId);
    public List<CtpAffair> getAffairsByAppsAndStatesAndMemberId(FlipInfo flipInfo,List<ApplicationCategoryEnum> appEnums,List<StateEnum> statesEnums,Long memberId);
	public List<CtpAffair> getAffairsByObjectId(long summaryId) throws BusinessException ;
    public CtpAffair getSimpleAffair(Long id) throws BusinessException;
    
    public List<CtpAffair> getAffairsByObjectIdAndStates(FlipInfo flipInfo,Long objectId,List<Integer> states) throws BusinessException;

    /**
     * 查找一条分库的数据，用作判断分库是否存数据
     * @return
     */
    public Integer getAffairHis();
    
    /**
     * 首页PORTAL同一流程只显示一条
     * @param string
     * @param parameter 
     * @param fi
     * @return
     */
    public Object getDeduplicationAffairList(final String sql, final Map<String, Object> parameter, final boolean onlyCount, final FlipInfo fi,String orderBySql) throws BusinessException;

	public List<CtpAffair> getAffairs(Long objectId, StateEnum state, SubStateEnum subState);
    
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
	public List<Long> getAllAffairIdByAppAndObjectId(ApplicationCategoryEnum appEnum, Long objectId);
	
	/**
	 * 分组查询事项状态
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public List getAffairDetailsBygorup(Map<String,Object> params) throws BusinessException ;
	
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
	
	public Integer countPendingAffairs(Map<String, Object> param) throws BusinessException;
	
	/**
	 *  查询指定时间段内，智能处理的记录，按处理人分组
	 * @param endTime 开始时间
	 * @param beginTime 结束时间
	 * @return eg {"-2637995837475110092",20}
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
	 * 根据节点权限获取affairs
	 * add by shenwei
	 * 20200724
	 * @param a
	 * @return
	 */
	public List<CtpAffair> getAffairsByNodePolicy(String pquanxian) throws BusinessException;
}
