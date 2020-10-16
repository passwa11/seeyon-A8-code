package com.seeyon.v3x.edoc.manager;

import java.util.List;

import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryRelation;
import com.seeyon.v3x.edoc.dao.EdocSummaryRelationDao;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
/**
 * 类描述：
 * 创建日期：
 *
 * @author puyc
 * @version 1.0 
 * @since JDK 5.0
 */
public class EdocSummaryRelationManagerImpl implements EdocSummaryRelationManager {
	
	private EdocSummaryRelationDao edocSummaryRelationDao;
	
	public void setEdocSummaryRelationDao(
			EdocSummaryRelationDao edocSummaryRelationDao) {
		
		this.edocSummaryRelationDao = edocSummaryRelationDao;
	}


	 /**
	  * puyc 保存类表 发文关联收文
	  */
    public void saveEdocSummaryRelation(EdocSummaryRelation  edocSummaryRelation){
    	this.edocSummaryRelationDao.save(edocSummaryRelation);
    }  
	/**
	 * 查找公文登记表
	 * relationEdocId 关联文公文Id
	 * relationEdocType 关联公文Type
	 * return EdocRegister
	 */
    public EdocRegister findEdocRegister(Long relationEdocId ,int relationEdocType){
    	return this.edocSummaryRelationDao.findEdocRegister(relationEdocId, relationEdocType);
    }
    /**
	 * 查找公文信息表
	 * relationEdocId 关联文公文Id
	 * relationEdocType 关联公文Type
	 * return EdocSummary
	 */
    public EdocSummary findEdocSummary(Long relationEdocId ,int relationEdocType){
    	return this.edocSummaryRelationDao.findEdocSummary(relationEdocId, relationEdocType);
    }
    
    /**
	 * 查找签收表
	 * relationEdocId 关联文公文Id
	 * return EdocRecieveRecord
	 */
    public EdocRecieveRecord findEdocRecieveRecord(Long relationEdocId){
    	return this.edocSummaryRelationDao.findEdocRecieveRecord(relationEdocId);
    }
    
    /**
     * 关联发文 
     * 收文Id
     * 收文Type
     * 发文的人员Id startMemberId
     */
    public List<EdocSummary>  findNewEdoc(Long relationEdocId, Long startMemberId, int edocType){
    	return this.edocSummaryRelationDao.findNewEdoc(relationEdocId, startMemberId, edocType);
    }
    
    public List<EdocSummary>  findAllNewEdoc(Long relationEdocId, Long startMemberId, int edocType){
        return this.edocSummaryRelationDao.findAllNewEdoc(relationEdocId, startMemberId, edocType);
    }
    
    
    /**
     * 收文转发文之后，查看关联发文 (只用于获得已登记或者待分发关联的发文) 
     * 收文Id
     * 收文Type
     * 发文的人员Id startMemberId
     * 已登记或者待分发fowardType
     */
    public List<EdocSummary>  findNewEdocByRegisteredOrWaitSent(Long relationEdocId, Long startMemberId, int edocType,int fowardType){
    	return this.edocSummaryRelationDao.findNewEdocByRegisteredOrWaitSent(relationEdocId, startMemberId, edocType,fowardType);
    }
    
    /**
	 * 关联收文  
	 * relationEdocId 发文Id
	 * 发文Type
	 */
	public EdocSummaryRelation  findRecEdoc(Long relationEdocId, int edocType){
		return this.edocSummaryRelationDao.findRecEdoc(relationEdocId, edocType);
	}
	  /**
     * @param list   在分发之前所有关联文的记录
     * @param summaryIdOld  原来发文的summaryId
     * @param summaryIdNew  收文形成之后的summaryId
     */
    public void updateEdocSummaryRelation(List<EdocSummaryRelation> list, Long summaryIdOld, Long summaryIdNew){
	   this.edocSummaryRelationDao.updateEdocSummaryRelation(list, summaryIdOld, summaryIdNew);
    }
  /**
   * 
   * @param summaryIdOld
   * @return   在分发之前所有关联文的记录
   */
	public List<EdocSummaryRelation> findEdocSummaryRelation(Long summaryIdOld){
		return this.edocSummaryRelationDao.findEdocSummaryRelation(summaryIdOld);
	}
	
	/**
     * 根据关联的收文id,去查找收文关联的所有发文 
     * relationEdocId 收文Id
     * 发文Type
     */
    public List<EdocSummaryRelation>  findRecEdocByRelationEdocId(Long relationEdocId, int edocType){
        return this.edocSummaryRelationDao.findRecEdocByRelationEdocId(relationEdocId,edocType);
    }
	
    public void updateEdocSummaryRelationList(List<EdocSummaryRelation> relationList){
        this.edocSummaryRelationDao.updateEdocSummaryRelationList(relationList);
    }

    @Override
    public List<EdocSummaryRelation> findRelationsBySummaryId(Long summaryId,
            int edocType) {
        return this.edocSummaryRelationDao.findRelationsBySummaryId(summaryId,edocType);
    }
}