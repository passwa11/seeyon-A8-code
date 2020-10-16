package com.seeyon.v3x.edoc.dao;

//import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocMark;
/**
 * Data access object (DAO) for domain model class EdocMark.
 * @see .EdocMark
 * @author MyEclipse - Hibernate Tools
 */
public class EdocMarkDAO extends BaseHibernateDao<EdocMark> {

    private static final Log log = LogFactory.getLog(EdocMarkDAO.class);	

    /**
     * 方法描述：保存公文文号
     */
    public void save(EdocMark edocMark) {
        log.debug("saving EdocMark instance");
        try {
            super.save(edocMark);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }
    
    /**
     * 判断文号是否被占用
     * @param edocId     公文id
     * @param EDOCMARK   文号
     * @return   true 被占用 false 未占用
     */
    public boolean isUsed(Long edocId){
    	
    	boolean used = false;    	
    	String hsql = "select count(*) as count from EdocMark as mark where mark.edocId=:edocId";
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	
    	List list = super.find(hsql, paramMap);
    	if (list != null && !list.isEmpty() && list.size() > 0) {
    		if (list.get(0) != null) {
    			int count = (Integer)list.get(0);    			
    			used = count > 0;    			
    		}
    	}
    	return used;
    }
    
    /**
     * 判断文号是否被占用
     * @param EDOCMARK   文号
     * @return   true 被占用 false 未占用
     */
    public boolean isUsed(String markstr,String edocId,String orgAccountId, String govdocType){
    	List<String> markstrList = new ArrayList<String>();
    	markstrList.add(markstr);
    	return isUsed(markstrList, edocId, orgAccountId, govdocType);
    }
    
    /**
     * 判断文号是否被占用
     * @param EDOCMARK   文号
     * @return   true 被占用 false 未占用
     */
    @SuppressWarnings("unchecked")
    public boolean isUsed(List<String> markstrList,String edocId,String orgAccountId, String govdocType){
    	
     	Long edocSummaryId=0L;
    	Long orgAcount = 0l;
    	if(Strings.isNotBlank(orgAccountId)) {
    	    orgAcount = Long.valueOf(orgAccountId);
    	}
    	boolean used = false; 
    	try{edocSummaryId=Long.parseLong(edocId);}catch(Exception e){}
    	String hsql = "select edocId as edocId from EdocMarkHistory as mark where mark.edocId<>:edocId and mark.edocId<>-1";
    	
    	if(govdocType!=null && Integer.parseInt(govdocType)==2) {//分办
    		hsql += " and govdocType="+ApplicationSubCategoryEnum.edoc_shouwen.key();
    	} else {
    		hsql += " and (govdocType!="+ApplicationSubCategoryEnum.edoc_fawen.key()+" or (transferStatus>=2 and govdocType="+ApplicationSubCategoryEnum.edoc_fawen.key()+"))";
    	}
    	
    	if(Strings.isNotEmpty(markstrList)) {
    		int i=0;
    		hsql += " and (";
    		for(String markstr : markstrList) {
    			markstr = markstr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	    	if(markstr != null){
    	    		markstr = SQLWildcardUtil.escape(markstr);
    	    	}
    			if(i != 0) {
    				hsql += " or ";
    			}
    			hsql += " docMark='"+markstr+"'";
    			i++;
    		}
    		hsql += ")";
    	}
    	
    	List<Long> edocIds = new ArrayList<Long>();
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	List<Object> list = (List<Object>)super.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		for(Object id : list){
    		    edocIds.add((Long)id);
    		}
    	}
    	
    	if(Strings.isNotEmpty(edocIds)){
    	    List<Long>[]  arr = Strings.splitList(edocIds, 1000);
    	    for(List<Long> _list : arr){
    	        used = isExsitEdocInAccount(_list,orgAcount);
    	        if(used){
    	            break;
    	        }
    	    }
    	}
    	return used;
    }
    
    /**
     * 判断文号是否被占用
     * @param EDOCMARK   文号
     * @return   true 被占用 false 未占用
     */
    @SuppressWarnings("unchecked")
    public boolean markIsUsed(List<String> markstrList,String edocId,String orgAccountId, String govdocType){
    	Long edocSummaryId=0L;
    	Long orgAcount = 0l;
    	if(Strings.isNotBlank(orgAccountId)) {
    	    orgAcount = Long.valueOf(orgAccountId);
    	}
    	boolean used = false; 
    	try{edocSummaryId=Long.parseLong(edocId);}catch(Exception e){}
    	String hsql = "select edocId as edocId from EdocMark as mark where status=110 and mark.edocId<>:edocId and mark.edocId<>-1";
    	
    	if(Strings.isNotEmpty(markstrList)) {
    		int i=0;
    		hsql += " and (";
    		for(String markstr : markstrList) {
    			markstr = markstr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	    	if(markstr != null){
    	    		markstr = SQLWildcardUtil.escape(markstr);
    	    	}
    			if(i != 0) {
    				hsql += " or ";
    			}
    			hsql += " docMark='"+markstr+"'";
    			i++;
    		}
    		hsql += ")";
    	}
    	
    	List<Long> edocIds = new ArrayList<Long>();
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	List<Object> list = (List<Object>)super.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		for(Object id : list){
    		    edocIds.add((Long)id);
    		}
    	}
    	
    	if(Strings.isNotEmpty(edocIds)){
    	    List<Long>[]  arr = Strings.splitList(edocIds, 1000);
    	    for(List<Long> _list : arr){
    	        used = isExsitEdocInAccount(_list,orgAcount);
    	        if(used){
    	            break;
    	        }
    	    }
    	}
    	return used;
    }

    
    private boolean isExsitEdocInAccount(List<Long> ids,Long orgAccountId){
        String hql = " from EdocSummary where id in (:ids) and orgAccountId = :orgAccountId and state in (0,1,3,112)";//112一个特殊数据的存在，表示公文归档过
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("ids", ids);
        params.put("orgAccountId", orgAccountId);
        int count = super.count(hql, params);
        return count > 0;
    }
    public List<EdocMark> findEdocMarkByCategoryId(Long categoryId){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("categoryId", categoryId);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.categoryId = :categoryId order by mark.docMarkNo", paramMap);
    	return list;
    }
    
    public List<EdocMark> findEdocMarkByCategoryId(Long categoryId,Integer docMarkNo){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("categoryId", categoryId);
    	paramMap.put("docMarkNo", docMarkNo);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.categoryId = :categoryId and mark.docMarkNo=:docMarkNo order by mark.docMarkNo", paramMap);
    	return list;
    }
    /**
     * 判断是否有其他公文使用与此流水（包括大流水和小流水）相关的文号
     * @param categoryId     流水ID
     * @param docMarkNo		 docMark表的文号的流水值
     * @param edocId		 公文ID
     * @return               true：有其他公文使用此流水，false:无其他公文使用此流水。
     */
    public boolean judgeOtherEdocUseCategroy(Long categoryId,Integer docMarkNo,Long edocId){
    	String hql="from EdocMark as mark where mark.categoryId = ? and mark.docMarkNo=? and edocId!=?";
    	return super.getQueryCount(hql, new Object[]{categoryId,docMarkNo,edocId}, new Type[]{Hibernate.LONG,Hibernate.INTEGER,Hibernate.LONG})>0;
    }
    public void deleteEdocMarkByEdocId(Long edocId) {
    	String hql="delete from EdocMark as mark where mark.edocId = :edocId";
    	Map<String,Object> nameParameters=new HashMap<String,Object>();
    	nameParameters.put("edocId", edocId);
    	super.bulkUpdate(hql, nameParameters);
    }
    public void deleteEdocMarkByCategoryIdAndNo(Long categoryId,Integer docMarkNo){
    	String hql="delete from EdocMark as mark where mark.categoryId = :categoryId and mark.docMarkNo=:docMarkNo";
    	Map<String,Object> nameParameters=new HashMap<String,Object>();
    	nameParameters.put("categoryId", categoryId);
    	nameParameters.put("docMarkNo",docMarkNo);
    	super.bulkUpdate(hql, nameParameters);
    }
    public List<EdocMark> findEdocMarkByEdocIdOrDocMark(Long edocId,String docMark){
    	if(docMark!=null){
    		docMark=SQLWildcardUtil.escape(docMark.trim());
    	}
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	paramMap.put("docMark", docMark);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId or mark.docMark=:docMark", paramMap);
    	return list;
    }
    
    public List<EdocMark> findEdocMarkByEdocIdOrDocMark(Long edocId,String docMark,String docMark2){
    	if(docMark!=null){
    		docMark=SQLWildcardUtil.escape(docMark.trim());
    	}
    	if(docMark2!=null){
    		docMark2=SQLWildcardUtil.escape(docMark2.trim());
    	}

    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocId);
    	paramMap.put("docMark", docMark);
    	paramMap.put("docMark2", docMark2);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId or mark.docMark=:docMark or mark.docMark=:docMark2", paramMap);
    	return list;
    }
    
    public List<EdocMark> findEdocMarkByMarkDefId(Long markDefId){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("markDefId", markDefId);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.status!=110 and mark.markDefId = :markDefId order by mark.docMarkNo", paramMap);
    	return list;
    }
    /**
     * 断号查询，去掉重复
     * @param markDefId
     * @return
     */
    public List<EdocMark> findEdocMarkByMarkDefId4Discontin(Long markDefId){
    	
    	List<EdocMark> list = findEdocMarkByMarkDefId(markDefId);
    	List<EdocMark> nlist = new ArrayList<EdocMark>();
    	Hashtable<String,String> hs=new Hashtable<String,String>();
    	
    	for(EdocMark em:list)
    	{
    		if(!hs.containsKey(em.getDocMark()))
    		{
    			hs.put(em.getDocMark(),em.getDocMark());
    			nlist.add(em);
    		}
    	}    	
    	return nlist;
    }
    
    public List<EdocMark> findEdocMarkByEdocSummaryId(Long edocSummaryId){
        //几个文号同时使用一个大流水，文号就存在多个
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId order by mark.docMarkNo", paramMap);
    	/*if(list!=null && list.size()>0)
    	{
    		return list.get(0);
    	}*/
    	return list;
    }
    /**
     * 根据公文文号和公文ID来查找edoc_mark表中的记录。
     * @param edocSummaryId 公文ID
     * @param edoc_mark	    公文文号
     * @param markNum	    联合发文的时候：第一套公文文号还是第二套。
     * @return
     */
    public EdocMark findEdocMarkByEdocSummaryIdAndEdocMark(Long edocSummaryId,String edocMark,int markNum){
    	if(edocMark!=null){
    		edocMark=SQLWildcardUtil.escape(edocMark.trim());
    	}
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	paramMap.put("edocMark", edocMark);
    	paramMap.put("markNum", markNum);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId and mark.docMark=:edocMark and mark.markNum=:markNum  order by mark.docMarkNo desc ", paramMap);
    	if(list!=null && list.size()>0)
    	{
    		return list.get(0);
    	}
    	return null;
    }
    public List<EdocMark> findEdocMarkByEdocSummaryIdAndNum(Long edocSummaryId,int markNum){
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", edocSummaryId);
    	paramMap.put("markNum", markNum);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId and mark.markNum=:markNum order by mark.docMarkNo", paramMap);
    	if(list!=null && list.size()>0)
    	{
    		return list;
    	}
    	return null;
    }
    public List<EdocMark> findMarkBySummaryId(Long summaryId, Integer markType) {
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocId", summaryId);
    	paramMap.put("markType", markType);
    	List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId and mark.markType=:markType order by mark.docMarkNo", paramMap);
    	if(list!=null && list.size()>0) {
    		return list;
    	}
    	return null;
    }
    
   public void deleteEdocMarkByIds(List<Long> ids){
	   String hql="delete from EdocMark where id in(:ids)";
	   Map<String,Object> nameParameters=new HashMap<String,Object>();
	   nameParameters.put("ids", ids);
	   super.bulkUpdate(hql, nameParameters);
   }

   public List<EdocMark> getEdocMarkByMemo(String[] memo,User user) {
	   Map<String, Object> paramMap = new HashMap<String, Object>();
   	   paramMap.put("domainId", user.getAccountId());
   	   paramMap.put("edocMark", SQLWildcardUtil.escape(memo[1].trim()));
   	   List<EdocMark> list = super.find("from EdocMark as mark where mark.domainId = :domainId and mark.status!=110 and mark.docMark=:edocMark  order by mark.docMarkNo", paramMap);
   	   return list;
   }
   
   public List<EdocMark> getAllEdocMark(String[] memo,Long summaryID) {
	   Map<String, Object> paramMap = new HashMap<String, Object>();
   	   paramMap.put("domainId", AppContext.currentAccountId());
   	   paramMap.put("edocMark", SQLWildcardUtil.escape(memo[1].trim()));
   	   paramMap.put("edocId", summaryID);
	   List<EdocMark> list = super.find("from EdocMark as mark where mark.domainId = :domainId and mark.docMark=:edocMark and mark.edocId != :edocId and mark.edocId !=-1 order by mark.docMarkNo", paramMap);
	   return list;
   }

	public void updateStatus(String[] memo,User user) {
		String hql = "update from EdocMark as mark set mark.status = 110 where mark.domainId = :domainId and mark.docMark=:docMark  order by mark.docMarkNo";
		Map<String, Object> paraMap = new HashMap<String, Object>();
	//	paraMap.put("defID", Long.valueOf(memo[0]));
		paraMap.put("domainId",user.getAccountId());
		paraMap.put("docMark", SQLWildcardUtil.escape(memo[1]));
		super.bulkUpdate(hql, paraMap);
	}

	public EdocMark getEdocMarkByEdocID(Long edocId) {
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new HashMap<String, Object>();
	   	paramMap.put("edocId", edocId);
		List<EdocMark> list = super.find("from EdocMark as mark where mark.edocId = :edocId order by createTime desc", paramMap);
    	if(list!=null && list.size()>0)
    	{
    		return list.get(0);
    	}
    	return null;
	}
	
	/**
     * 判断文号是否被占用
     * @param EDOCMARK   文号
     * @return   true 被占用 false 未占用
     */
    @SuppressWarnings("unchecked")
    public boolean isGovdocUsedNew(String markType, String govdocType, String markStr,String edocId,String orgAccountId) {
    	Long edocSummaryId = 0L;
    	Long orgAcount = 0l;
    	if(Strings.isNotBlank(orgAccountId)) {
    	    orgAcount = Long.valueOf(orgAccountId);
    	}
    	boolean used = false; 
    	try{edocSummaryId=Long.parseLong(edocId);}catch(Exception e){}
    	String hsql = "select edocId as edocId from EdocMarkHistory as mark where mark.docMark=:edocMark and mark.edocId<>:edocId and mark.edocId<>-1";
    	
    	if(govdocType!=null && Integer.parseInt(govdocType)==2) {//分办
    		hsql += " and govdocType="+ApplicationSubCategoryEnum.edoc_shouwen.key();
    	} else {
    		hsql += " and (govdocType!="+ApplicationSubCategoryEnum.edoc_fawen.key()+" or (transferStatus>=2 and govdocType="+ApplicationSubCategoryEnum.edoc_fawen.key()+"))";
    	}
    	
    	markStr = markStr.replaceAll(String.valueOf((char)160), String.valueOf((char)32));
    	
    	if(markStr != null) {
    		markStr = SQLWildcardUtil.escape(markStr);
    	}
    	List<Long> edocIds = new ArrayList<Long>();
    	
    	Map<String, Object> paramMap = new HashMap<String, Object>();
    	paramMap.put("edocMark", markStr);
    	paramMap.put("edocId", edocSummaryId);
    	List<Object> list = (List<Object>)super.find(hsql, paramMap);
    	
    	if (Strings.isNotEmpty(list)) {
    		for(Object id : list){
    		    edocIds.add((Long)id);
    		}
    	}
    	
    	if(Strings.isNotEmpty(edocIds)) {
    	    List<Long>[]  arr = Strings.splitList(edocIds, 1000);
    	    for(List<Long> _list : arr){
    	        used = isExsitEdocInAccount(_list,orgAcount);
    	        if(used){
    	            break;
    	        }
    	    }
    	}
    	return used;
    }
    
    /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-公文文号对象的最小创建时间]start*/
    public EdocMark getMinCreaateDateEdocMark(){
    	String hsql = "from EdocMark as mark order by createTime asc";
		List<EdocMark> list = super.find(hsql, 0, 1, new HashMap<String,Object>(), new Object[0]);
		if(list!=null && list.size()>0){
    		return list.get(0);
    	}
    	return null;
    }
    /*客开 项目名称：贵州市政府-G6V580省级专版 作者：mtech 修改日期：2017-08-11 [修改功能：公文库-公文文号对象的最小创建时间]end*/
	
    public EdocMark getMaxMark(Long edocId){
    	String hql="from EdocMark as mark where mark.categoryId=(select categoryId from EdocMark where edocId=:edocId) and mark.edocId !=:edocId and mark.edocId!=-1  order by docMarkNo desc";
    	Map<String,Object> param=new HashMap<String,Object>();
    	param.put("edocId", edocId);
    	List<EdocMark> find = super.find(hql, 0, 1, param, new ArrayList<Object>() );
    	if(find!=null&&!find.isEmpty()){
    		return find.get(0);
    	}
    	return null;
    }
}