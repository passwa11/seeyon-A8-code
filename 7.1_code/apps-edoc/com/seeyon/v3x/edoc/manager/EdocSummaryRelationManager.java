package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocSummaryRelation;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;

/**
 * 收文和发文相互关联表业务层
 */
public interface EdocSummaryRelationManager {
	
	 
     /**
	  * puyc 保存类表 发文关联收文
	  */
	public void saveEdocSummaryRelation(EdocSummaryRelation edocSummaryRelation);
	/**
	 * 收文转发文之后，发文关联收文  ， 查找公文登记表
	 * relationEdocId 关联文公文Id
	 * relationEdocType 关联公文Type
	 * return EdocRegister
	 */
	public EdocRegister findEdocRegister(Long relationEdocId,int relationEdocType);
	/**
	 * 查找公文信息表
	 * relationEdocId 关联文公文Id
	 * relationEdocType 关联公文Type
	 * return EdocSummary
	 */
	public EdocSummary findEdocSummary(Long relationEdocId ,int relationEdocType);
	/**
	 * 查找签收表
	 * relationEdocId 关联文公文Id
	 * return EdocRecieveRecord
	 */
    public EdocRecieveRecord findEdocRecieveRecord(Long relationEdocId);
    /**
     * 收文转发文之后，查看关联发文 
     * 收文Id
     * 收文Type
     * 发文的人员Id startMemberId
     */
    public List<EdocSummary>  findNewEdoc(Long relationEdocId, Long startMemberId, int edocType);
    
    public List<EdocSummary>  findAllNewEdoc(Long relationEdocId, Long startMemberId, int edocType);
    
    
    
    /**
     * 收文转发文之后，查看关联发文 (只用于获得已登记或者待分发关联的发文) 
     * 收文Id
     * 收文Type
     * 发文的人员Id startMemberId
     * 已登记或者待分发fowardType
     */
    public List<EdocSummary>  findNewEdocByRegisteredOrWaitSent(Long relationEdocId, Long startMemberId, int edocType,int fowardType);
    
    /**
	 * 收文转发文之后，发文关联收文  
	 * relationEdocId 发文Id
	 * 发文Type
	 */
	public EdocSummaryRelation  findRecEdoc(Long relationEdocId, int edocType);
	 /**
	  * 分发后，更新所有关联文的记录
    * @param list  分发前所有关联文的记录
    * @param summaryIdOld  原来发文的summaryId
    * @param summaryIdNew  收文形成之后的summaryId
    */
	public void updateEdocSummaryRelation(List<EdocSummaryRelation> list, Long summaryIdOld, Long summaryIdNew);
	/**
	 * 查询在分发之前所有关联文的记录
	 * @param summaryIdOld
	 * @return   在分发之前所有关联文的记录
	 */
	public List<EdocSummaryRelation> findEdocSummaryRelation(Long summaryIdOld);
	/**
     * 根据关联的收文id,去查找收文关联的所有发文 
     * relationEdocId 收文Id
     * 发文Type
     */
	public List<EdocSummaryRelation>  findRecEdocByRelationEdocId(Long relationEdocId, int edocType);
	
	public void updateEdocSummaryRelationList(List<EdocSummaryRelation> relationList);
	
	/**
     * 根据被关联ID获取关联数据
     * @Author      : xuqiangwei
     * @Date        : 2014年8月25日下午4:32:30
     * @param relationEdocId
     * @param edocType
     * @return
     */
	public List<EdocSummaryRelation> findRelationsBySummaryId(Long summaryId, int edocType);
}
