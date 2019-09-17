package com.seeyon.v3x.edoc.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.UUIDLong;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocBodyDao extends BaseHibernateDao<EdocBody> {

	public void saveOrUpdate(EdocBody po) {
		super.getHibernateTemplate().clear();
		super.getHibernateTemplate().flush();
		DBAgent.saveOrUpdate(po);
		/*if(DBAgent.get(EdocBody.class, po.getId()) != null) {
			super.update(po);
		} else {
			super.save(po);	
		}*/
	}
	
	public EdocBody getBodyByIdAndNum(String summaryId,int contentNum)
	{
		Object[] values = new Object[]{Long.valueOf(summaryId),Integer.valueOf(contentNum)};
		String hql="from EdocBody as body where body.edocId = ? and body.contentNo= ? ";
		List<EdocBody> list = getHibernateTemplate().find(hql,values);
		if (list.size() == 0) {return null;}
		else
		{
			return list.get(0);
		}
	}
	
	public List<EdocBody> getBodysById(Long summaryId)
	{
		Object[] values = new Object[]{Long.valueOf(summaryId)};
		String hql="from EdocBody as body where body.edocId = ?  ";
		List<EdocBody> list = getHibernateTemplate().find(hql,values);
		return list;
	}
	/**
	 * 查出某条公文对应的所有的正文对象,如果数据库总存在多条记录，每种类型的只取一条。
	 * @param summaryId
	 * @return
	 */
	public List<EdocBody> getBodyByIdAndNum(Long summaryId)
	{
		Object[] values = new Object[]{Long.valueOf(summaryId)};
		String hql="select id,content,contentType,contentName,createTime,lastUpdate,contentStatus,contentNo " +
				"from EdocBody as body where body.edocId = ?  ";
		List<Object[]> list = getHibernateTemplate().find(hql,values);
		List<EdocBody> nl=new ArrayList<EdocBody>();
		boolean hasZero=false; //0
		boolean hasOne=false;  //1
		boolean hasTwo=false;//2
		if (list.size() == 0) {return null;}
		else
		{
			for(Object[] o:list){
				EdocBody eb=new EdocBody();
				eb.setId((Long)o[0]);
				eb.setContent((String)o[1]);
				eb.setContentType((String)o[2]);
				eb.setContentName((String)o[3]);
				if(o[4]!=null)
					eb.setCreateTime(new Timestamp(((java.util.Date)o[4]).getTime()));
				if(o[5]!=null)
					eb.setLastUpdate(new Timestamp(((java.util.Date)o[5]).getTime()));
				eb.setContentStatus((Integer)o[6]);
				eb.setContentNo((Integer)o[7]);
				EdocSummary edocSummary=new EdocSummary();
				edocSummary.setId(summaryId);
				eb.setEdocSummary(edocSummary);
				
				
				if(eb.getContentNo()==0&&!hasZero){ 
					nl.add(eb);
					hasZero=true;
				}
				if(eb.getContentNo()==1&&!hasOne){
					nl.add(eb);
					hasOne=true;
				}
				if(eb.getContentNo()==2&&!hasTwo){
					nl.add(eb);
					hasTwo=true;
				}
			}
			return nl;
		}
	}
	/**
	 * 拟文正文套红的时候直接添加新的edocBody对象。
	 * @param summaryId
	 * @param contentNum
	 * @param bodyType
	 * @return
	 */
	public String createContentNum(String summaryId,int contentNum,String bodyType)
	{
		try{
		
		EdocBody nb=new EdocBody();
		nb.setIdIfNew();
		nb.setContent(Long.toString(UUIDLong.longUUID()));
		nb.setContentNo(contentNum);
		nb.setContentType(bodyType);
		nb.setCreateTime(new Timestamp(System.currentTimeMillis()));
			
		EdocSummary summary=new EdocSummary();
		summary.setId(Long.valueOf(summaryId));
		nb.setEdocSummary(summary);
			
		nb.setLastUpdate(new Timestamp(System.currentTimeMillis()));
		super.save(nb);		
		return nb.getContent();
		}catch(Exception e)
		{
			return "";
		}
	}
	public String createContentNum(String summaryId,int contentNum)
	{
		try{
		EdocBody eb=getBodyByIdAndNum(summaryId,0);
		EdocBody nb=new EdocBody();
		nb.setIdIfNew();
		nb.setContent(Long.toString(UUIDLong.longUUID()));
		String contentName=eb.getContentName();
		if(contentName==null || "".equals(contentName)){contentName=eb.getContent();}
		nb.setContentName(contentName);
		nb.setContentNo(contentNum);
		nb.setContentStatus(eb.getContentStatus());
		nb.setContentType(eb.getContentType());
		nb.setCreateTime(eb.getCreateTime());
		nb.setEdocSummary(eb.getEdocSummary());
		nb.setLastUpdate(eb.getLastUpdate());
		super.save(nb);		
		return nb.getContent();
		}catch(Exception e)
		{
			return "";
		}
	}

	/**
	 * 获取公文的正文列表
	 * @Author      : xuqw
	 * @Date        : 2015年8月26日下午3:22:02
	 * @param summaryId
	 * @return
	 */
    public List<EdocBody> findEdocBodys(long summaryId) {
        Object[] values = new Object[] { Long.valueOf(summaryId) };
        String hql = "select id,content,contentType,contentName,createTime,lastUpdate,contentStatus,contentNo "
                + "from EdocBody as body where body.edocId = ?  ";
        List<Object[]> list = getHibernateTemplate().find(hql, values);
        List<EdocBody> nl = new ArrayList<EdocBody>();
        if (list.size() == 0) {
            return null;
        } else {
            for (Object[] o : list) {
                EdocBody eb = new EdocBody();
                eb.setId((Long) o[0]);
                eb.setContent((String) o[1]);
                eb.setContentType((String) o[2]);
                eb.setContentName((String) o[3]);
                if (o[4] != null)
                    eb.setCreateTime(new Timestamp(((java.util.Date) o[4])
                            .getTime()));
                if (o[5] != null)
                    eb.setLastUpdate(new Timestamp(((java.util.Date) o[5])
                            .getTime()));
                eb.setContentStatus((Integer) o[6]);
                eb.setContentNo((Integer) o[7]);
                EdocSummary edocSummary = new EdocSummary();
                edocSummary.setId(summaryId);
                eb.setEdocSummary(edocSummary);
                nl.add(eb);
            }
            return nl;
        }
    }
}
