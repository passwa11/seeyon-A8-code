package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.domain.EdocSummaryRelation;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;

public class EdocSummaryRelationDao extends BaseHibernateDao<EdocSummaryRelation> {
	 private static final Log log = LogFactory.getLog(EdocSummaryRelationDao.class);
	 /**
	  * puyc 保存类表 发文关联收文
	  */
	public void save(EdocSummaryRelation  edocSummaryRelation){
		super.getHibernateTemplate().save(edocSummaryRelation);
	}
	
	public List<EdocSummary>  findAllNewEdoc(Long relationEdocId, Long startMemberId, int edocType){
//	    String hsql = "select edocSummary,affair.id from EdocSummaryRelation as esr, EdocSummary as edocSummary,CtpAffair affair " +
//	            " where esr.summaryId = ? and edocSummary.startUserId = ? and esr.edocType = ? and  esr.relationEdocId = edocSummary.id "+
//	            " and affair.objectId = edocSummary.id and (affair.state = 2 or affair.state = 1)  and edocSummary.state !=?";
		String hsql = "select edocSummary,affair.id from EdocSummaryRelation as esr, EdocSummary as edocSummary,CtpAffair affair " +
		        " where esr.summaryId = ? and esr.edocType = ? and  esr.relationEdocId = edocSummary.id "+
		        " and affair.objectId = edocSummary.id and (affair.state = 2 or affair.state = 1)  and edocSummary.state !=?";
	            Object[] values = {relationEdocId, edocType,4};
	            List list = super.findVarargs(hsql, values);
	            List<EdocSummary> sumlist = new ArrayList<EdocSummary>();
	            for(int i=0;i<list.size();i++){
	                   Object[] obj = (Object[])list.get(i);
	                   EdocSummary summary = (EdocSummary)obj[0];
	                   Long affairId = (Long)obj[1];
	                   summary.setAffairId(affairId);
	                   sumlist.add(summary);
	               }
	            if(list.size() == 0){
                    return null;
                }else{
                    return sumlist;
                }
	}
	
	
	 /**
     * 收文转发文之后，查看关联发文 
     * 收文Id
     * 收文Type
     * 发文的人员Id startMemberId
     */
	public List<EdocSummary>  findNewEdoc(Long relationEdocId, Long startMemberId, int edocType){

		try{
			  // String hsql = "select edocSummary from EdocSummaryRelation as esr, EdocSummary as edocSummary where esr.summaryId = ? and edocSummary.startUserId = ? and esr.edocType = ? and  esr.relationEdocId = edocSummary.id";
			   //Object[] values = {relationEdocId, startMemberId, edocType};
			
		    List<EdocSummary> sumlist = new ArrayList<EdocSummary>();
		    
			//GOV-4483  新建收文中新建的阅文，在待阅中转发文，关联发文、关联收文均勾选  
			//修改查询affair表的待发和已发状态
//			String hsql = "select edocSummary,affair.id from EdocSummaryRelation as esr, EdocSummary as edocSummary,CtpAffair affair " +
//			" where esr.summaryId = ? and edocSummary.startUserId = ? and esr.edocType = ? and  esr.relationEdocId = edocSummary.id "+
//			" and affair.objectId = edocSummary.id and (affair.state = ? or affair.state = ?) and affair.app = ? and affair.delete = false and edocSummary.state !=?"+
//			" and esr.type is null "; 
		    String hsql = "select edocSummary,affair.id from EdocSummaryRelation as esr, EdocSummary as edocSummary,CtpAffair affair " +
			" where esr.summaryId = ?  and esr.edocType = ? and  esr.relationEdocId = edocSummary.id "+
			" and affair.objectId = edocSummary.id and (affair.state = ? or affair.state = ?) and affair.app = ? and affair.delete = false and edocSummary.state !=?"+
			" and esr.type is null "; 
			
	       Object[] values = {relationEdocId, edocType,StateEnum.col_sent.key(),StateEnum.col_waitSend.key()
	    		   ,ApplicationCategoryEnum.edocSend.key(),4};
	           List list = super.findVarargs(hsql, values);
	           
	           for(int i=0;i<list.size();i++){
	               Object[] obj = (Object[])list.get(i);
	               EdocSummary summary = (EdocSummary)obj[0];
	               Long affairId = (Long)obj[1];
	               summary.setAffairId(affairId);
	               sumlist.add(summary);
	           }
	           
//			   List<EdocSummary> list = super.find(hsql, values);
			  if(list.size() == 0){
						return null;
					}else{
						return sumlist;
					}
			}catch(RuntimeException re){
				log.error("", re);
				return null;
			}
	}
	
	
	
	public List<EdocSummary>  findNewEdocByRegisteredOrWaitSent(Long relationEdocId, Long startMemberId, int edocType,int forwardType){

		try{ 
//			 String hsql = "select edocSummary,affair.id from EdocSummaryRelation as esr, EdocSummary as edocSummary,CtpAffair affair " +
//			" where esr.summaryId = ? and edocSummary.startUserId = ? and esr.edocType = ? and  esr.relationEdocId = edocSummary.id "+
//			"  and esr.type = ? "+
			//原来的 sql为什么要加上edocSummary.startUserId = ? 限制，这样只有发起人才能看到关联的发文
			String hsql = "select edocSummary,affair.id from EdocSummaryRelation as esr, EdocSummary as edocSummary,CtpAffair affair " +
			" where esr.summaryId = ? and esr.edocType = ? and  esr.relationEdocId = edocSummary.id "+
			"  and esr.type = ? "+
			" and affair.objectId = edocSummary.id and (affair.state = ? or affair.state = ?) and affair.app = ? and affair.delete = false and edocSummary.state !=? ";
			 List<EdocSummary> sumlist = new ArrayList<EdocSummary>();
			 
			 
	       Object[] values = {relationEdocId, edocType,forwardType,StateEnum.col_sent.key(),StateEnum.col_waitSend.key()
                   ,ApplicationCategoryEnum.edocSend.key(),4};

    	       List list = super.findVarargs(hsql, values);
               
               for(int i=0;i<list.size();i++){
                   Object[] obj = (Object[])list.get(i);
                   EdocSummary summary = (EdocSummary)obj[0];
                   Long affairId = (Long)obj[1];
                   summary.setAffairId(affairId);
                   sumlist.add(summary);
               }
			  if(list.size() == 0){
						return null;
					}else{
						return sumlist;
					}
			}catch(RuntimeException e){
				log.error("",e);
				return null;
			}
	}
	
	/**
	 * 收文转发文之后，发文关联收文  
	 * relationEdocId 发文Id
	 * 发文Type
	 */
	public EdocSummaryRelation  findRecEdoc(Long relationEdocId, int edocType){
		try{
			String hsql = "select esr from EdocSummaryRelation as esr where esr.summaryId = ? and esr.edocType = ?";   
			Object[] values = {relationEdocId, edocType};
			   List<EdocSummaryRelation> list = super.findVarargs(hsql, values);
			  if(list.size() == 0){
						return null;
					}else{
						return list.get(0);
					}
			}catch(RuntimeException e){
				log.error("",e);
				return null;
			}
	}
	/**
	 * 收文转发文之后，发文关联收文  ， 查找公文登记表
	 * relationEdocId 关联文公文Id
	 * relationEdocType 关联公文Type
	 * return EdocRegister
	 */
	public EdocRegister findEdocRegister(Long relationEdocId,int relationEdocType){
		try{
			   String hsql = "select edocRegister from EdocRegister as edocRegister where edocRegister.id = ? and edocRegister.edocType = ?";//注意这里传递的是edocRegister表的id，而不是edocid
			   Object[] values = {relationEdocId, relationEdocType};
			   List<EdocRegister> list = super.findVarargs(hsql, values);
			  if(list.size() == 0){
						return null;
					}else{
						return list.get(0);
					}
			}catch(RuntimeException e){
				log.error("", e);
				return null;
			}
	}
	/**
	 * 查找公文信息表
	 * relationEdocId 关联文公文Id
	 * relationEdocType 关联公文Type
	 * return EdocSummary
	 */
	public EdocSummary findEdocSummary(Long relationEdocId,int relationEdocType){
		try{
			   String hsql = "select edocSummary from EdocSummary as edocSummary where edocSummary.id = ? and edocSummary.edocType = ?";
			   Object[] values = {relationEdocId, relationEdocType};
			   List<EdocSummary> list = super.findVarargs(hsql, values);
			  if(list.size() == 0){
						return null;
					}else{
						return list.get(0);
					}
			}catch(RuntimeException e){
				log.error("", e);
				return null;
			}
	}
	
	/**
	 * 查找签收表
	 * relationEdocId 关联文公文Id
	 * return EdocRecieveRecord
	 */
	public EdocRecieveRecord findEdocRecieveRecord(Long relationEdocId){
		try{
			   String hsql = "select edocRecieveRecord from EdocRecieveRecord as edocRecieveRecord  where edocRecieveRecord.edocId = ?";
			   Object[] values = {relationEdocId};
			   List<EdocRecieveRecord> list = super.findVarargs(hsql, values);
			  if(list.size() == 0){
						return null;
					}else{
						return list.get(0);
					}
			}catch(RuntimeException e){
				log.error("", e);
				return null;
			}
	}

	 /**
	  * 分发后，更新所有关联文的记录
     * @param list  分发前所有关联文的记录
     * @param summaryIdOld  原来发文的summaryId
     * @param summaryIdNew  收文形成之后的summaryId
     */
	public void updateEdocSummaryRelation(List<EdocSummaryRelation> list, Long summaryIdOld, Long summaryIdNew){
		if(list != null && summaryIdNew != null){
			for (int i = 0; i < list.size(); i++) {
				EdocSummaryRelation esr=(EdocSummaryRelation)list.get(i);
				if(summaryIdOld.longValue() == esr.getSummaryId().longValue()){
					esr.setSummaryId(summaryIdNew);
				}
				else if(summaryIdOld.longValue() == esr.getRelationEdocId().longValue()){
					esr.setRelationEdocId(summaryIdNew);
				}
				super.update(esr);
			}
		}
	}
	/**
	 * 查询在分发之前所有关联文的记录
	 * @param summaryIdOld
	 * @return   在分发之前所有关联文的记录
	 */
	public List<EdocSummaryRelation> findEdocSummaryRelation(Long summaryIdOld){
		try{
		 String hsql = "select edocSummaryRelation from EdocSummaryRelation as edocSummaryRelation  where edocSummaryRelation.summaryId = ? or edocSummaryRelation.relationEdocId = ?";
		 Object[] values = {summaryIdOld,summaryIdOld};
		 List<EdocSummaryRelation> list = super.findVarargs(hsql, values);
		if(list.size() == 0){
			return null;
		}else{
			return list;
		}
      }catch(RuntimeException e){
    	  log.error("", e);
    	  return null;
      }
	}
	
	
	/**
     * 根据关联的收文id,去查找收文关联的所有发文 
     * relationEdocId 收文Id
     * 发文Type
     */
    public List<EdocSummaryRelation>  findRecEdocByRelationEdocId(Long relationEdocId, int edocType){
        try{
            String hsql = "select esr from EdocSummaryRelation as esr where esr.relationEdocId = ? and esr.edocType = ?";   
            Object[] values = {relationEdocId, edocType};
               List<EdocSummaryRelation> list = super.findVarargs(hsql, values);
               return list;
            }catch(RuntimeException e){
              log.error("", e);
                return null;
            }
    }
    
    
    public void updateEdocSummaryRelationList(List<EdocSummaryRelation> relationList){
        super.updatePatchAll(relationList);
    }
    
    /**
     * 根据被关联ID获取关联数据
     * @Author      : xuqiangwei
     * @Date        : 2014年8月25日下午4:32:30
     * @param relationEdocId
     * @param edocType
     * @return
     */
    public List<EdocSummaryRelation> findRelationsBySummaryId(Long summaryId, int edocType){
        try{
            String hsql = "select esr from EdocSummaryRelation as esr where esr.summaryId = ? and esr.edocType = ?";   
            Object[] values = {summaryId, edocType};
               @SuppressWarnings("unchecked")
            List<EdocSummaryRelation> list = super.findVarargs(hsql, values);
               return list;
            }catch(RuntimeException re){
                return null;
            }
    }
	
}





