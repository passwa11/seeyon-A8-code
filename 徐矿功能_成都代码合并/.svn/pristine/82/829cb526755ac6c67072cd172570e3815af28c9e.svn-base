package com.seeyon.apps.xkjt.dao.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.Strings;

import com.seeyon.apps.xkjt.dao.XkjtDao;
import com.seeyon.apps.xkjt.po.EdocFormInfo;
import com.seeyon.apps.xkjt.po.XkjtLeaderDaiYue;
import com.seeyon.apps.xkjt.po.XkjtOpenMode;
import com.seeyon.apps.xkjt.po.XkjtSummaryAttachment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;

public class XkjtDaoImpl implements XkjtDao {
	
	private static final org.apache.commons.logging.Log LOGGER = LogFactory.getLog(XkjtDaoImpl.class);

	@Override
	public void saveOpenMode(XkjtOpenMode xkjtOpenMode)throws BusinessException {
		DBAgent.save(xkjtOpenMode);
	}

	@Override
	public List findOpenModeByNodeId(Long nodeId) throws BusinessException {
		String hql = "from XkjtOpenMode where nodeId =:nodeId and isDeleted=0";
		Map param = new HashMap();
		param.put("nodeId",nodeId);
		List list = DBAgent.find(hql, param);
		return list;
	}

	@Override
	public void updateOpenMode(XkjtOpenMode xkjtOpenMode)throws BusinessException {
		DBAgent.update(xkjtOpenMode);
	}

	@Override
	public void saveMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException {
		DBAgent.save(xkjtSummaryAttachment);
	}

	@Override
	public XkjtSummaryAttachment findMainAttachmentBySummaryId(Long summaryId) throws BusinessException {
		String hql = "from XkjtSummaryAttachment where summaryId =:summaryId";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("summaryId", summaryId);
		List xkjtSummaryAttachments = DBAgent.find(hql, param);
		if(xkjtSummaryAttachments.size()!=0){
			XkjtSummaryAttachment xkjtSummaryAttachment = (XkjtSummaryAttachment) xkjtSummaryAttachments.get(0);
			return xkjtSummaryAttachment;
		}else{
			return null;
		}
		
	}

	@Override
	public void updateMainAttachment(XkjtSummaryAttachment xkjtSummaryAttachment) throws BusinessException {
		DBAgent.update(xkjtSummaryAttachment);
	}

	@Override
	public boolean isMainAttachment(Long attachmentId,Long summaryId) throws BusinessException {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("attachmentId", attachmentId);
		param.put("summaryId", summaryId);
		String hql = "from XkjtSummaryAttachment where attachmentId =:attachmentId and summaryId =:summaryId";
		List xkjtSummaryAttachments = DBAgent.find(hql, param);
		if(xkjtSummaryAttachments.size()>0){
			return true;
		}else{
			return false;
		}
		
	}

	@Override
	public void saveXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue)throws BusinessException {
		DBAgent.save(xkjtLeaderDaiYue);
		
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocId(Long edocId,Long leaderId)
			throws BusinessException {
		String hql = "from XkjtLeaderDaiYue where edocId=:edocId and leaderId=:leaderId and status=11";
		Map params = new HashMap<String, Object>();
		params.put("edocId", edocId);
		params.put("leaderId", leaderId);
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = DBAgent.find(hql, params);
		return xkjtLeaderDaiYues;
	}

	/**项目：徐州矿物集团【待阅栏目：待阅栏目显示的内容，标题+文号+时间】 作者：wxt.xiangrui 时间：2019-6-3 start*/
	@Override
	public List<Object> findXkjtLeaderDaiYueByMemberId(Long memberId)throws BusinessException {
		/*
		 * String hql =
		 * "from XkjtLeaderDaiYue where leaderId=:leaderId and status=11 order by sendDate desc"
		 * ; Map params = new HashMap<String, Object>(); params.put("leaderId",
		 * memberId);
		 * 
		 * List<XkjtLeaderDaiYue> xkjtLeaderaDiYues = DBAgent.find(hql, params);
		 */
		String hql = "SELECT xkjt.ID,xkjt.EDOC_ID,xkjt.TITLE,xkjt.SEND_DATE,edoc.DOC_MARK FROM XKJT_LEADER_DAIYUE xkjt,EDOC_SUMMARY edoc ";
		hql+=" WHERE EDOC_ID = edoc.ID AND xkjt.LEADER_ID = "+memberId+" AND status = 11 ORDER BY xkjt.SEND_DATE DESC";
		List<Object> xkjtLeaderDaiYues =new ArrayList<Object>();
		JDBCAgent jdbcAgent=new JDBCAgent(true);
		try {
			jdbcAgent.execute(hql);
			xkjtLeaderDaiYues=jdbcAgent.resultSetToList();
		} catch (SQLException e) {
			LOGGER.error("待阅栏目获取异常：",e);
		}finally{
            jdbcAgent.close();
        }
		return xkjtLeaderDaiYues;
	}
	/**项目：徐州矿物集团【待阅栏目：待阅栏目显示的内容，标题+文号+时间】 作者：wxt.xiangrui 时间：2019-6-3 end*/
	
	@Override
	public void updateXkjtLeaderDaiYue(XkjtLeaderDaiYue xkjtLeaderDaiYue) throws BusinessException {
		DBAgent.update(xkjtLeaderDaiYue);
	}

	@Override
	public FlipInfo findMoreXkjtLeaderDaiYueByMemberId(FlipInfo fi,Map params) throws BusinessException {
		String hql = "from XkjtLeaderDaiYue where leaderId=:leaderId";
		if (params.get("title") != null) {
			hql += " and title like :title";
		}
		hql += " and status=11 order by sendDate desc";
		DBAgent.find(hql, params, fi);
		return fi;
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByMemberId(Long memberId,Long edocId) throws BusinessException {
		String hql = "from XkjtLeaderDaiYue where leaderId=:leaderId and edocId=:edocId and status=11 order by sendDate desc";
		Map params = new HashMap<String, Object>();
		params.put("leaderId", memberId);
		params.put("edocId", edocId);
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = DBAgent.find(hql, params);
		return xkjtLeaderDaiYues;
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueByEdocIdAndSendId(Long sendRecordId, Long edocId) throws BusinessException {
		String hql = "from XkjtLeaderDaiYue where edocId=:edocId and sendRecordId=:sendRecordId";
		Map params = new HashMap<String, Object>();
		params.put("edocId", edocId);
		params.put("sendRecordId", sendRecordId);
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = DBAgent.find(hql, params);
		return xkjtLeaderDaiYues;
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderDaiYueById(Long id) {
		String hql = "from XkjtLeaderDaiYue where id=:id";
		Map params = new HashMap<String, Object>();
		params.put("id", id);
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = DBAgent.find(hql, params);
		return xkjtLeaderDaiYues;
	}

	@Override
	public List<XkjtLeaderDaiYue> findXkjtLeaderYiYueByMemberId(Long memberId) throws BusinessException {
		String hql = "from XkjtLeaderDaiYue where leaderId=:leaderId and status=12 order by sendDate desc";
		Map params = new HashMap<String, Object>();
		params.put("leaderId", memberId);
		List<XkjtLeaderDaiYue> xkjtLeaderDaiYues = DBAgent.find(hql, params);
		return xkjtLeaderDaiYues;
	}

	@Override
	public FlipInfo findMoreXkjtLeaderYiYueByMemberId(FlipInfo fi, Map params) throws BusinessException {
		String hql = "from XkjtLeaderDaiYue where leaderId=:leaderId";
		if (params.get("title") != null) {
			hql += " and title like :title";
		}
		hql += " and status=12 order by sendDate desc";
		DBAgent.find(hql, params, fi);
		return fi;
	}

	/**项目：徐州矿物集团【办结栏目】 作者：wxt.xiangrui 时间：2019-5-29 start*/
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId) throws BusinessException {
		String hql = "SELECT summary.id,	summary.state,	summary.COMPLETE_TIME,summary.DOC_MARK,	summary.EDOC_TYPE,	summary.SEND_UNIT,	summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID FROM  CTP_AFFAIR  affair,EDOC_SUMMARY  summary  WHERE affair.MEMBER_ID = "+memberId;
		hql+=" 	AND summary.ID = affair.OBJECT_ID  AND summary.STATE = 3 	AND affair.state = 4 	AND affair.IS_DELETE = 0  ORDER BY	summary.CREATE_TIME DESC";
		List<Object> xkjtLeaderBanJie =new ArrayList<Object>();
		JDBCAgent jdbcAgent=new JDBCAgent(true);
		try {
			jdbcAgent.execute(hql);
			xkjtLeaderBanJie=jdbcAgent.resultSetToList();
		} catch (SQLException e) {
			LOGGER.error("办结栏目获取异常：",e);
		}finally{
            jdbcAgent.close();
        }
		/*
		 * Map params = new HashMap<String, Object>(); params.put("memberId", memberId);
		 * 
		 * xkjtLeaderBanJie =DBAgent.find(hql, params); Object
		 * o=xkjtLeaderBanJie.getClass();
		 */
		return xkjtLeaderBanJie;
	}
	/**项目：徐州矿物集团【办结栏目】 作者：wxt.xiangrui 时间：2019-5-29 end*/
	
	@Override
	public List<Object> findXkjtLeaderBanJieByMemberId(Long memberId, String templetIds) throws BusinessException {
		//徐矿 办结同一条流程显示一条 zelda 2019年12月7日09:25:46 start
		String hql = "SELECT summary.id,	summary.state,	summary.COMPLETE_TIME,summary.DOC_MARK,	summary.EDOC_TYPE,	summary.SEND_UNIT,	summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID FROM  CTP_AFFAIR  affair,EDOC_SUMMARY  summary,(select max(affair2.id) as id from ctp_affair affair2 where 1=1 and affair2.member_ID = " + memberId + " AND affair2.state = 4  AND affair2.IS_DELETE = 0 GROUP BY affair2.OBJECT_ID ) affair2 WHERE affair2.id = affair.id and affair.MEMBER_ID = "+memberId;
		//徐矿 办结同一条流程显示一条 zelda 2019年12月7日09:25:46 end
		hql+=" 	AND summary.ID = affair.OBJECT_ID  AND summary.STATE = 3 	AND affair.state = 4 	AND affair.IS_DELETE = 0 ";
				
		String orderBy =" ORDER BY	summary.CREATE_TIME DESC";
		
		if(Strings.isNotBlank(templetIds) && !"null".equals(templetIds)){
			hql += " AND summary.TEMPLETE_ID IN (" + templetIds + ")";
		}
		
		hql += orderBy;
		
		List<Object> xkjtLeaderBanJie =new ArrayList<Object>();
		JDBCAgent jdbcAgent=new JDBCAgent(true);
		try {
			jdbcAgent.execute(hql);
			xkjtLeaderBanJie=jdbcAgent.resultSetToList();
		} catch (SQLException e) {
			LOGGER.error("办结栏目获取异常：",e);
		}finally{
            jdbcAgent.close();
        }
		/*
		 * Map params = new HashMap<String, Object>(); params.put("memberId", memberId);
		 * 
		 * xkjtLeaderBanJie =DBAgent.find(hql, params); Object
		 * o=xkjtLeaderBanJie.getClass();
		 */
		return xkjtLeaderBanJie;
	}
	
	/**项目：徐州矿物集团【办结栏目更多页加条件查询】 作者：wxt.xiangrui 时间：2019-5-29 start*/
	@Override
	public FlipInfo findMoreXkjtLeaderBanJieByMemberId(FlipInfo fi, Map<String,Object> params) throws BusinessException {
		List<Object> xkjtLeaderBanJie =new ArrayList<Object>();
		StringBuffer hqlSb=new StringBuffer();
		//hqlSb.append( "from XkjtLeaderBanJie summary where");
		//hqlSb.append(" select summary.id,summary.urgentLevel,summary.startUserId,summary.state,summary.deadlineDatetime,summary.completeTime,summary.docMark,summary.edocType,summary.sendUnit,summary.subject,summary.createPerson,summary.createTime from XkjtLeaderBanJie2 as affair,XkjtLeaderBanJie as summary where affair.memberId=:memberId AND summary.id=affair.objectId  AND summary.state=3 AND affair.state=4 AND affair.isDelete=0 ");
		//hqlSb.append(" summary.startUserId=:memberId");
		//hqlSb.append(" AND summary.state=3");
		//徐矿 办结同一条流程显示一条 zelda 2019年12月7日09:25:46 start
		String hql = "SELECT summary.id,	summary.state,summary.URGENT_LEVEL,	summary.COMPLETE_TIME,summary.DOC_MARK,	summary.EDOC_TYPE,	summary.SEND_UNIT,	summary.SUBJECT, summary.CREATE_PERSON,summary.CREATE_TIME,summary.START_USER_ID FROM CTP_AFFAIR  affair,EDOC_SUMMARY  summary, (select max(affair2.id) as id from ctp_affair affair2 where 1=1 and affair2.member_ID = " + params.get("memberId") + " AND affair2.state = 4  AND affair2.IS_DELETE = 0 GROUP BY affair2.OBJECT_ID ) affair2 WHERE affair2.id = affair.id and affair.MEMBER_ID = "+params.get("memberId");
		//徐矿 办结同一条流程显示一条 zelda 2019年12月7日09:25:46 end
		hql+=" 	AND summary.ID=affair.OBJECT_ID  AND summary.STATE = 3 	AND affair.state = 4 	AND affair.IS_DELETE = 0  ";
		hqlSb.append(hql);
		//标题
		if(params.get("title")!=null) {
			String title1=(String) params.get("title");
			params.remove("title");
			params.put("title","%"+title1+"%");
			hqlSb.append(" AND summary.SUBJECT like '"+params.get("title")+"'");
		}
		//重要程度
		if(params.get("importLevel")!=null) {
			hqlSb.append(" AND summary.URGENT_LEVEL ="+params.get("importLevel"));
		}
		//发起人
		if(params.get("sender")!=null) {
			String sender1=(String) params.get("sender");
			params.remove("sender");
			params.put("sender","%"+sender1+"%");
			hqlSb.append(" AND summary.CREATE_PERSON like '"+params.get("sender")+"'");
		}
		//发起开始时间
		if(params.get("beginTime")!=null) {
			hqlSb.append(" AND summary.CREATE_TIME >=to_date('"+params.get("beginTime")+"','yyyy-mm-dd')");
		}
		//发起结束时间
		if(params.get("endTime")!=null) {
			hqlSb.append(" AND summary.CREATE_TIME <=to_date('"+params.get("endTime")+"','yyyy-mm-dd')");
		}
		//处理开始时间
		if(params.get("dealBeginTime")!=null) {
			hqlSb.append(" AND summary.COMPLETE_TIME >=to_date('"+params.get("dealBeginTime")+"','yyyy-mm-dd')");
		}
		//处理结束时间
		if(params.get("dealEndTime")!=null) {
			hqlSb.append(" AND summary.COMPLETE_TIME <=to_date('"+params.get("dealEndTime")+"','yyyy-mm-dd')");
		}
		//徐矿 增减按照模板选择 zelda 2019年12月7日09:25:46 start
		if(params.get("templetIds") != null) {
			hqlSb.append(" AND summary.TEMPLETE_ID IN (" + params.get("templetIds") + ")");
		}
		hqlSb.append("");
		//徐矿 增减按照模板选择 zelda 2019年12月7日09:25:46 end
		hqlSb.append(" ORDER BY	summary.CREATE_TIME DESC");
		JDBCAgent jdbcAgent=new JDBCAgent(true);
		try {
			jdbcAgent.execute(hqlSb.toString());
			xkjtLeaderBanJie=jdbcAgent.resultSetToList();
		} catch (SQLException e) {
			LOGGER.error("办结栏目更多页加条件查询获取异常：",e);
		}finally{
            jdbcAgent.close();
        }
		//分页
		int page = fi.getPage();
		int size = fi.getSize();
		
		fi.setTotal(xkjtLeaderBanJie.size());
		//使用list进行分页---因使用自定义的类进行查询出来的数据无key，所以使用SQL进行查询
		List newList =new ArrayList();
		   int currIdx = (page > 1 ? (page - 1) * size : 0);
		   for (int i = 0; i < size && i < xkjtLeaderBanJie.size() - currIdx; i++) {
			   newList.add(xkjtLeaderBanJie.get(currIdx + i));
		   }
		fi.setData(newList);
		return fi;
	}
	/**项目：徐州矿物集团【办结栏目更多页加条件查询】 作者：wxt.xiangrui 时间：2019-5-29 end*/
	
	@Override
	public EdocFormInfo getByFormId(Long formId) {
		EdocFormInfo info = null;
		String hql = "from EdocFormInfo where formId =:formId";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("formId", formId);
		@SuppressWarnings("unchecked")
		List<EdocFormInfo> list = DBAgent.find(hql, params);
		if (list != null && list.size() == 1) {
			info = list.get(0);
		}
		return info;
	}
	
	@Override
	public void updateEdocFormInfo(EdocFormInfo info) {
		DBAgent.update(info);
	}
	
	@Override
	public void saveEdocFormInfo(EdocFormInfo info) {
		DBAgent.save(info);		
	}

	@Override
	public void saveOpenModes(List<XkjtOpenMode> xkjtOpenModes) throws BusinessException {
		DBAgent.saveAll(xkjtOpenModes);
	}

}
