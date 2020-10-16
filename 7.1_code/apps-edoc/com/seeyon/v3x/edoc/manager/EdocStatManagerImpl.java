package com.seeyon.v3x.edoc.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.v3x.edoc.dao.EdocStatDao;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.domain.EdocStatCondObj;
import com.seeyon.v3x.edoc.domain.EdocStatCondition;
import com.seeyon.v3x.edoc.domain.EdocStatDisObj;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.statistics.ContentHandler;
import com.seeyon.v3x.edoc.manager.statistics.StatisticsContentTypeEnum;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.EdocStatHelper;

public class EdocStatManagerImpl implements EdocStatManager {
	private static final Log log = LogFactory.getLog(EdocStatManagerImpl.class);
	
	private EdocStatDao edocStatDao;	
	private OrgManager orgManager;
	private EnumManager metadataManagerNew;
	
	public void setMetadataManagerNew(EnumManager metadataManagerNew) {
		this.metadataManagerNew = metadataManagerNew;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public EdocStatDao getEdocStatDao() {
		return edocStatDao;
	}
	public void setEdocStatDao(EdocStatDao edocStatDao) {
		this.edocStatDao = edocStatDao;
	}
	
	/**
     * 创建统计数据
     * @Author      : xuqw
     * @Date        : 2015年6月5日下午2:31:44
     * @param summary
     * @param isUnit 是否为联合发文
     * @param bodyType, 区分联合发文第一发文，还是第二发文， 例如，EdocBody.EDOC_BOBY_NORMAL
     * @throws Exception
     */
    private void createEdocStat(EdocSummary summary, boolean isUnit, int bodyType, User user) throws Exception {
        
        long edocId = summary.getId();
        int edocType = summary.getEdocType();
        String subject = null;
        String docMark = null;
        String sendTo = null;
        String copyTo = null;
        Integer copies = null;
        
        if(isUnit){
            
            if(bodyType == EdocBody.EDOC_BODY_FIRST){
                
                subject = summary.getSubjectA();
                docMark = summary.getDocMark();
                sendTo = summary.getSendTo();
                copyTo = summary.getCopyTo();
                copies = summary.getCopies();
                
            }else {//EdocBody.EDOC_BODY_SECOND
                
                subject = summary.getSubjectB();
                docMark = summary.getDocMark2();
                sendTo = summary.getSendTo2();
                copyTo = summary.getCopyTo2();
                copies = summary.getCopies2();
            }
            
        }else {
            subject = summary.getSubject();
            docMark = summary.getDocMark();
            sendTo = summary.getSendTo();
            copyTo = summary.getCopyTo();
            copies = summary.getCopies();
        }
        String docType = summary.getDocType();
        String createDate = Datetimes.formatDate(summary.getCreateTime());
        
        long deptId = user.getDepartmentId();
        
        
        String issuer = summary.getIssuer();
        
        int flowState = summary.getState();
        long domainId = user.getAccountId();
        int contentNo = bodyType;
        String serialNo = summary.getSerialNo();
        long createrUserId = summary.getStartUserId();
        long accountId = summary.getOrgAccountId();
        long orgDepartmentId = summary.getOrgDepartmentId();
        String sendType = summary.getSendType();
        String unitLevel = summary.getUnitLevel(); 
        Long templeteId = summary.getTempleteId();
        
            if(edocId == 0){
                throw new Exception(ResourceUtil.getString("edoc.error.id.empty"));//公文id为空！
            }       
            if(edocType < 0){
                throw new Exception(ResourceUtil.getString("edoc.error.type.empty"));//公文类型为空！
            }
            if(subject == null || "".equals(subject) || "".equals(subject)){
                throw new Exception(ResourceUtil.getString("edoc.error.subject.empty"));//公文标题为空！
            }
            if(createDate == null || "".equals(createDate) || "".equals(createDate)){
                throw new Exception(ResourceUtil.getString("edoc.error.createDate.empty"));//建文日期为空！
            }
            if(deptId == 0){
                throw new Exception(ResourceUtil.getString("edoc.error.deptId.empty"));//部门id为空！
            }
            if(domainId == 0){
                throw new Exception(ResourceUtil.getString("edoc.error.domainId.empty"));//统计单位id为空！
            }
            if(createDate.trim().length()>10){
                throw new Exception(ResourceUtil.getString("edoc.error.createDate.format") + "yyyy-mm-dd");//传入建文日期不符合格式，应为：
            }
            String docTypes = docType;
            if(null==docType || "".equals(docType)){
                docTypes = "-1";
            }
            EdocStat edocStat = new EdocStat();
            edocStat.setIdIfNew();
            edocStat.setEdocId(edocId);
            if(summary.getGovdocType()!=null && summary.getGovdocType().intValue()==4) {
            	edocStat.setEdocType(summary.getGovdocType());	
            } else {
            	edocStat.setEdocType(edocType);	
            }
            edocStat.setSubject(subject);
            edocStat.setDocType(docTypes);
            edocStat.setDocMark(docMark);
            edocStat.setDeptId(deptId);
            edocStat.setCreateDate(summary.getCreateTime());
            edocStat.setIsArchived(false);
            edocStat.setIsSent(false);
            edocStat.setSendTo(sendTo);
            edocStat.setCopyTo(copyTo);
            edocStat.setIssuer(issuer);
            edocStat.setCopies(copies);
            edocStat.setFlowState(flowState);
            edocStat.setDomainId(domainId);
            edocStat.setYear(getYear(createDate));
            edocStat.setMonth(getMonth(createDate));
            edocStat.setQuarter(getQuarter(createDate));
            edocStat.setDay(getDay(createDate));
            edocStat.setContentNo(contentNo);
            edocStat.setAccountId(Long.valueOf(accountId));
            edocStat.setOrgDepartmentId(Long.valueOf(orgDepartmentId));
            edocStat.setCreateUserid(Long.valueOf(createrUserId)) ;
            edocStat.setSerialNo(serialNo) ;
            edocStat.setSendType(sendType);
            edocStat.setUnitLevel(unitLevel);
            edocStat.setTempleteId(templeteId);
            try{
            edocStatDao.save(edocStat);
            }catch(Exception e)
            {
                log.error("公文统计保存错误",e);
                throw e;
            }
        }
	
	/**
	 * 取年
	 * @param date
	 * @return
	 */
	private String getYear(String date){
		String [] datearray = date.split("-");
		return datearray[0];
	}
	/**
	 * 取月
	 * @param date
	 * @return
	 */
	private  String getMonth(String date){
		String [] datearray = date.split("-");
		return datearray[1];
	}
	private  String getQuarter(String date) {
		return String.valueOf(EdocStatHelper.getQuarter(Integer.parseInt(getMonth(date))));
	}	
	private  String getDay(String date){
		String [] datearray = date.split("-");
		return datearray[2];
	}	
	public void updateEdocStat(EdocStat edocStat) throws Exception {
		edocStatDao.update(edocStat);
	}
	
	public EdocStat getEdocStat(long id) {
		return edocStatDao.get(id);
	}
	
	public EdocStat getEdocStatByEdocId(long edocId) {
		return edocStatDao.findUniqueBy("edocId", edocId);		
	}	
	
	public List<EdocStat> getEdocStatsByEdocId(long edocId)
	{
		return edocStatDao.findBy("edocId",edocId);
	}
	
	/**
	 * 输入查询条件，查询发文统计记录。
	 * @param edocType 公文类型
	 * @param flowState 公文流转状态（流转中、已封发）
	 * @param beginDate 开始日期（建文日期）
	 * @param endDate 结束日期（建文日期）
	 * @param deptIds 部门集合
	 * @param domainId 单位id
	 * @param needPagination 是否需要分页
	 * @return List<EdocStat>
	 */
	public List<EdocStat> querySentEdocStat(int flowState, Date beginDate, Date endDate, Collection<Long> deptIds, long domainId,boolean needPagination) {
		
		StringBuilder sb = new StringBuilder("from EdocStat es where es.edocType = " + EdocEnum.edocType.sendEdoc.ordinal());
        Map namedParameters=new HashMap();
        if (flowState == 0) {
			sb.append(" and es.flowState = :flowState ");
			namedParameters.put("flowState", flowState);
		}else if(flowState == 3) {
			sb.append(" and es.isSent=true");
		}
		if (beginDate != null) {
			sb.append(" and es.createDate >= :beginDate ");
			namedParameters.put("beginDate", beginDate);
		}
		if (endDate != null) {
			sb.append(" and es.createDate <= :endDate ");
			namedParameters.put("endDate", endDate);
		}
		if (domainId != 0) {
			sb.append(" and es.domainId = :domainId ");
			namedParameters.put("domainId", domainId);
		}
		else if (deptIds != null &&	deptIds.size()!=0) {
			sb.append(" and es.deptId in (:deptIds)");
			namedParameters.put("deptIds", deptIds);
		}
		else {
			sb.append(" and 1=0");
		}
		sb.append(" order by es.createDate desc");	
		
		if(needPagination){
			return edocStatDao.queryEdocStat(sb.toString(), namedParameters);	
		}else{
			return  edocStatDao.queryEdocStatAll(sb.toString(), namedParameters);	
		}
	}
	public List<EdocStat> querySentEdocStatAll(int flowState, Date beginDate, Date endDate, long deptId, long domainId) {
		List<Long> deptIds=new ArrayList<Long>();
		deptIds.add(deptId);
		return querySentEdocStat(flowState,beginDate,endDate,deptIds,domainId,true);
	}
	// 输入查询条件，查询发文统计记录
	public List<EdocStat> querySentEdocStat(int flowState, Date beginDate, Date endDate, long deptId, long domainId) {
		List<Long> deptIds=new ArrayList<Long>();
		deptIds.add(deptId);
		return querySentEdocStat(flowState,beginDate,endDate,deptIds,domainId,false);
	}
	
	
	// 输入查询条件，查询收文、签报统计记录
	public List<EdocStat> queryEdocStat(int edocType, Date beginDate, Date endDate, Collection<Long> deptIds, long domainId,boolean needPagination) {
		StringBuilder sb = new StringBuilder("from EdocStat es where es.edocType = :edocType ");
		Map namedParameters=new HashMap();
		namedParameters.put("edocType", edocType);
		List<Type> types = new ArrayList<Type>();
		if (beginDate != null) {
			sb.append(" and es.createDate >= :beginDate");
			namedParameters.put("beginDate", beginDate);
		}
		if (endDate != null) {	
			sb.append(" and es.createDate <= :endDate");
			namedParameters.put("endDate", endDate);
		}		
		if (domainId != 0) {
			sb.append(" and es.domainId = :domainId");
			namedParameters.put("domainId", domainId);
		}
		else if (deptIds != null &&	deptIds.size()!=0) {
			sb.append(" and es.deptId in (:deptIds)");
			namedParameters.put("deptIds", deptIds);
		}
		else {
			sb.append(" and 1=0");
		}
		sb.append(" order by es.createDate desc");

		if(needPagination){
			return edocStatDao.queryEdocStat(sb.toString(), namedParameters);	
		}else{
			return edocStatDao.queryEdocStatAll(sb.toString(), namedParameters);	
		}		
	}
	public List<EdocStat> queryEdocStat(int edocType, Date beginDate, Date endDate, long deptId, long domainId) {
		List<Long> deptIds=new ArrayList<Long>();
		deptIds.add(deptId);
		return queryEdocStat(edocType,beginDate,endDate,deptIds,domainId,true);
	}
	public List<EdocStat> queryEdocStatAll(int edocType, Date beginDate, Date endDate, long deptId, long domainId) {
		List<Long> deptIds=new ArrayList<Long>();
		deptIds.add(deptId);
		return queryEdocStat(edocType,beginDate,endDate,deptIds,domainId,false);
	}
//	 输入查询条件，查询公文归档记录
	public List<EdocStat> queryArchivedEdocStat(Date beginDate, Date endDate, Collection<Long> deptIds, long domainId,boolean needPagination) {
		//获取插件的类所在的包路径更换
	    boolean edocPlugin = com.seeyon.ctp.common.SystemEnvironment.hasPlugin("edoc");
		StringBuilder sb = new StringBuilder("from EdocStat es where es.isArchived = " + true);
		Map namedParameters=new HashMap();
		if (beginDate != null) {
			sb.append(" and es.archivedTime >= :beginDate");
			namedParameters.put("beginDate", beginDate);
		}
		if (endDate != null) {
			sb.append(" and es.archivedTime <= :endDate");
			namedParameters.put("endDate", endDate);
		}
		if (domainId != 0) {
			sb.append(" and es.domainId = :domainId");
			namedParameters.put("domainId", domainId);
		}
		else if (deptIds != null &&	deptIds.size()!=0) {
			sb.append(" and es.deptId in (:deptIds)");
			namedParameters.put("deptIds", deptIds);
		}
		//OA-21146  公文统计---收发查询--归档统计，统计不到数据    下面的条件明显有问题啊 注释掉
//		else {
//			sb.append(" and 1=0");
//		}
		if(!edocPlugin){
			sb.append(" and es.edocType = 2");
		}
		sb.append(" order by es.createDate desc");
		
		if(needPagination){
			return edocStatDao.queryEdocStat(sb.toString(), namedParameters);	
		}else{
			return edocStatDao.queryEdocStatAll(sb.toString(), namedParameters);	
		}		
		
	}
	// 输入查询条件，查询公文归档记录
	public List<EdocStat> queryArchivedEdocStat(Date beginDate, Date endDate, long deptId, long domainId) {
		List<Long> deptIds=new ArrayList<Long>();
		deptIds.add(deptId);
		return queryArchivedEdocStat(beginDate,endDate,deptIds,domainId,true);
	}
	
	public List<EdocStat> queryArchivedEdocStatAll(Date beginDate, Date endDate, long deptId, long domainId) {
		List<Long> deptIds=new ArrayList<Long>();
		deptIds.add(deptId);
		return queryArchivedEdocStat(beginDate,endDate,deptIds,domainId,false);
	}
	
	public List<EdocStatDisObj> statEdoc(EdocStatCondObj esco, int groupType) {		
		String queryCondition = parseCondition(esco);
		Hashtable<String,Integer> hashtable1 = null;
		Hashtable<String,Integer> hashtable2 = null;
		Hashtable<String,Integer> hashtable3 = null;
		Hashtable<String,EdocStatDisObj> hashtable = new Hashtable<String,EdocStatDisObj>();
		Enumeration<String> enumeration = null;
		// 处理收文统计记录
		boolean edocPlugin = com.seeyon.ctp.common.SystemEnvironment.hasPlugin("edoc");
		if(edocPlugin){//判断有无公文插件
		hashtable1 = edocStatDao.getEdocStatResult(EdocEnum.edocType.recEdoc.ordinal(), queryCondition, groupType);
		enumeration = hashtable1.keys();
		while (enumeration.hasMoreElements()) {
			EdocStatDisObj esdo = new EdocStatDisObj();
			String key = enumeration.nextElement();
			Integer recNum = hashtable1.get(key);
			esdo.setRecieveNum(recNum);
			hashtable.put(key, esdo);
		}
		// 处理发文统计记录
		hashtable2 = edocStatDao.getEdocStatResult(EdocEnum.edocType.sendEdoc.ordinal(), queryCondition, groupType);
		enumeration = hashtable2.keys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			EdocStatDisObj esdo = hashtable.get(key);
			if (esdo == null) {
				esdo = new EdocStatDisObj();								
			}
			Integer sendNum = hashtable2.get(key);
			esdo.setSendNum(sendNum);
			hashtable.put(key, esdo);
		}
		}
		// 处理签报统计记录
		hashtable3 = edocStatDao.getEdocStatResult(EdocEnum.edocType.signReport.ordinal(), queryCondition, groupType);
		enumeration = hashtable3.keys();
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			EdocStatDisObj esdo = hashtable.get(key);
			if (esdo == null) {
				esdo = new EdocStatDisObj();								
			}
			Integer signNum = hashtable3.get(key);
			esdo.setSignNum(signNum);
			hashtable.put(key, esdo);
		}
		
		List<EdocStatDisObj> results = new ArrayList<EdocStatDisObj>();
		EdocStatDisObj esdo1 = new EdocStatDisObj();
		EdocStatDisObj otherObj = new EdocStatDisObj();
		
		esdo1.setColumnName("edoc.stat.total.label");
		otherObj.setColumnName("edoc.stat.other.label");
		
		enumeration = hashtable.keys();
		
		boolean byTypeAndNoDocType = false;  //定义一个变量，用于表示是否按照公文种类来查询，而且没有公文种类
		
		// --- circle start ---
		while (enumeration.hasMoreElements()) {	
			boolean noDocType = false; //定义一个变量，由于表示是否按照公文种类来查询，而且没有公文种类（循环内使用）
			String key = enumeration.nextElement();
			EdocStatDisObj esdo = hashtable.get(key);
			if (esdo == null) {
				esdo = new EdocStatDisObj();
			}
			if (groupType == Constants.EDOC_STAT_GROUPBY_DEPT) {
				String deptName = "";
				try {
					deptName = orgManager.getDepartmentById(Long.valueOf(key)).getName();
				}
				catch (Exception e) {
				}				
				esdo.setColumnName(deptName);
				if(edocPlugin){//判断有无公文插件
				esdo.setTotalNum(esdo.getRecieveNum() + esdo.getSendNum() + esdo.getSignNum());
				}else{
					esdo.setTotalNum(esdo.getSignNum());					
				}
			}
			else {				
				String docType = metadataManagerNew.getEnumItemLabel(EnumNameEnum.edoc_doc_type, key);
				if(null!=docType && !"".equals(docType)){//如果公文种类不为空，那么正常保存
					esdo.setColumnName(docType);
					if(edocPlugin){//判断有无公文插件					
					esdo.setTotalNum(esdo.getRecieveNum() + esdo.getSendNum() + esdo.getSignNum());	
					}else{
						esdo.setTotalNum(esdo.getSignNum());							
					}
				}else{//如果公文种类为空，那么在 “其他” 这一记录条目上添加数据
					byTypeAndNoDocType = true;
					noDocType = true;
					otherObj.setRecieveNum(otherObj.getRecieveNum()+ esdo.getRecieveNum());
					otherObj.setSendNum(otherObj.getSendNum() + esdo.getSendNum());
					otherObj.setSignNum(otherObj.getSignNum() + esdo.getSignNum());					
					if(edocPlugin){//判断有无公文插件		
					otherObj.setTotalNum(otherObj.getTotalNum() + esdo.getRecieveNum() + esdo.getSendNum() + esdo.getSignNum());
					}else{
						otherObj.setTotalNum(otherObj.getTotalNum() + esdo.getSignNum());						
					}
				}
			}
			if(noDocType!=true){//docType不为空，那么添加记录，并且在累加总计,反之则不做此操作
				if(edocPlugin){//判断有无公文插件						
				esdo.setTotalNum(esdo.getRecieveNum() + esdo.getSendNum()+ esdo.getSignNum());
				}else{
					esdo.setTotalNum(esdo.getSignNum());					
				}
				esdo1.setRecieveNum(esdo1.getRecieveNum() + esdo.getRecieveNum());
				esdo1.setSendNum(esdo1.getSendNum() + esdo.getSendNum());
				esdo1.setSignNum(esdo1.getSignNum() + esdo.getSignNum());
				esdo1.setTotalNum(esdo1.getTotalNum() + esdo.getTotalNum());
				results.add(esdo);
			}
		}
		//--- circle end ---
		
		if(byTypeAndNoDocType){//跳出循环后，如果发现循环中含有 “其他” 栏目的数据，在results上添加 “其他”，并将总计的数据累加上去
			results.add(otherObj);
			esdo1.setRecieveNum(esdo1.getRecieveNum() + otherObj.getRecieveNum());
			esdo1.setSendNum(esdo1.getSendNum() + otherObj.getSendNum());
			esdo1.setSignNum(esdo1.getSignNum() + otherObj.getSignNum());
			esdo1.setTotalNum(esdo1.getTotalNum() + otherObj.getTotalNum());			
		}
		results.add(esdo1);
		
		return results;
	}
		
	
	/**
	 * 用于解析出条件表达式
	 * @param esdo
	 * @return
	 */
	private String parseCondition(EdocStatCondObj esco){
		StringBuilder sb = new StringBuilder();
		
		sb.append(" and es.year= " + esco.getYear());
		
		if (esco.getPeriodType() == Constants.EDOC_STAT_PERIOD_TYPE_MONTH) {
			sb.append(" and es.month= " + esco.getMonth());
		}
		else if (esco.getPeriodType() == Constants.EDOC_STAT_PERIOD_TYPE_SEASON) {
		
			if(esco.getSeason() == 1){
				sb.append(" and es.month >= 1 and es.month <= 3 ");
			}
			else if(esco.getSeason() == 2){
				sb.append(" and es.month >= 4 and es.month <= 6 ");
			}
			else if(esco.getSeason() == 3){
				sb.append(" and es.month >= 7 and es.month <= 9 ");
			}
			else if(esco.getSeason() == 4){
				sb.append(" and es.month >= 10 and es.month <= 12 ");
			}			
		}	
		if (esco.getDomainId() != 0) {
			sb.append(" and es.domainId=" + esco.getDomainId());
		}
		else if (esco.getDeptIds() != null && esco.getDeptIds().size()!=0) {
			StringBuilder s=new StringBuilder();
			s.append("(");
			for(Long l:esco.getDeptIds()){
				if(!"(".equals(s.toString()))
					s.append(",");
				s.append(l);
			}
			s.append(")");
			sb.append(" and es.deptId in " + s.toString());
		}
		else {
			sb.append(" and 1=0");
		}
//		if (esco.getDeptId() != 0) {
//			sb.append(" and es.deptId=" + esco.getDeptId());
//		}
				
		return sb.toString();
	}
	
	/**
	 * 根据公文对象生产统计记录
	 * @param summary
	 * @param user
	 * @throws Exception
	 */
	public void createState(EdocSummary summary,User user) throws Exception
	{
		if(summary.getIsunit()){
			createEdocStat(summary, true, EdocBody.EDOC_BODY_FIRST, user);
			createEdocStat(summary, true, EdocBody.EDOC_BODY_SECOND, user);
		} else {
		    createEdocStat(summary, false, EdocBody.EDOC_BOBY_NORMAL, user);
		}
	}
	
	
	
	/**
	 * 公文元素修改后，修改对应的统计数据记录
	 * @param summary
	 * @throws Exception
	 */
	public void updateElement(EdocSummary summary) throws Exception
	{
		if(summary.getIsunit())
		{
			List <EdocStat> edocStats= getEdocStatsByEdocId(summary.getId());
			if(edocStats!=null && edocStats.size()==2)
			{
				EdocStat edocStat=edocStats.get(0);
				setEdocStatValue(edocStat,summary.getSubjectA(),summary.getDocMark(),summary.getDocType(),summary.getSendTo(),summary.getCopyTo(),summary.getIssuer(),summary.getCopies(),EdocBody.EDOC_BODY_FIRST, summary.getSendType(), summary.getUnitLevel(), summary.getTempleteId());
				updateEdocStat(edocStat);
				
				edocStat=edocStats.get(1);
				setEdocStatValue(edocStat,summary.getSubjectB(),summary.getDocMark2(),summary.getDocType(),summary.getSendTo2(),summary.getCopyTo2(),summary.getIssuer(),summary.getCopies2(),EdocBody.EDOC_BODY_SECOND, summary.getSendType(), summary.getUnitLevel(), summary.getTempleteId());
				updateEdocStat(edocStat);				
			}			
		}
		else
		{
			EdocStat edocStat= getEdocStatByEdocId(summary.getId());
			if(edocStat==null){return;}
			setEdocStatValue(edocStat,summary.getSubject(),summary.getDocMark(),summary.getDocType(),summary.getSendTo(),summary.getCopyTo(),summary.getIssuer(),summary.getCopies(),EdocBody.EDOC_BOBY_NORMAL, summary.getSendType(), summary.getUnitLevel(), summary.getTempleteId());
			updateEdocStat(edocStat);
		}		
	}
	private void setEdocStatValue(EdocStat edocStat,String subject,String docMark,String docType,String sendTo,String copyTo,String issuer,Integer copies,int contentNo, String sendType, String unitLevel, Long templeteId)
	{
		if(edocStat==null){return;}
		edocStat.setSubject(subject);
		edocStat.setDocMark(docMark);
		edocStat.setDocType(docType);
		edocStat.setSendTo(sendTo);
		edocStat.setCopyTo(copyTo);
		edocStat.setIssuer(issuer);
		edocStat.setCopies(copies);
		edocStat.setContentNo(contentNo);
		edocStat.setSendType(sendType);
		edocStat.setUnitLevel(unitLevel);
		edocStat.setTempleteId(templeteId);
	}
	/**
	 * 更新流程状态
	 * @param edocId
	 * @param flowState
	 * @throws Exception
	 */
	public void updateFlowState(Long edocId,int flowState) throws Exception
	{
		/*
		EdocStat edocStat= getEdocStatByEdocId(edocId);
		edocStat.setFlowState(flowState);
		updateEdocStat(edocStat);
		*/
		Map <String,Object> columns=new HashMap<String,Object>();		
		columns.put("flowState",flowState);
		edocStatDao.update("edocId",edocId, columns);
	}
	/**
	 * 设置为封发
	 * @param edocId
	 * @throws Exception
	 */
	public void setSeal(Long edocId) throws Exception
	{
		/*
		EdocStat edocStat= getEdocStatByEdocId(edocId);
		edocStat.setIsSent(true);
		edocStat.setFlowState(Constant.flowState.finish.ordinal());//封发后将流程状态置为结束!
		updateEdocStat(edocStat);
		*/
		Map <String,Object> columns=new HashMap<String,Object>();
		columns.put("isSent", true);
		columns.put("flowState", EdocConstant.flowState.finish.ordinal());//封发后将流程状态置为结束!
		edocStatDao.update("edocId",edocId, columns);
	}
	/**
	 * 设置为归档
	 * @param edocId
	 * @throws Exception
	 */
	public void setArchive(Long edocId) throws Exception
	{
		//EdocStat edocStat= getEdocStatByEdocId(edocId);
		//edocStat.setIsArchived(true);
		//updateEdocStat(edocStat);
		Map <String,Object> columns=new HashMap<String,Object>();
		columns.put("isArchived", true);	
		columns.put("archivedTime", new Date()) ;
		edocStatDao.update("edocId",edocId, columns);
	}
	
	/**
	 * 保存公文备考信息。
	 * @param id 公文统计记录id
	 * @param remark 备考信息
	 * @throws Exception
	 */
	public void saveEdocRemark(Long id, String remark) throws Exception {
		EdocStat edocStat = this.edocStatDao.get(id);
		edocStat.setRemark(remark);
		this.edocStatDao.update(edocStat);
	}
	
	public void deleteEdocStat(Long summaryId)throws Exception{
		
		edocStatDao.delete(new String[]{"edocId"},new Object[]{summaryId});
	}
	
	public List getStatContent(int statContentId,long loginAccount){
		StatisticsContentTypeEnum typeEnum = StatisticsContentTypeEnum.valueOf(statContentId);
		ContentHandler handler = typeEnum.getContentHandlerInstance();
		List list = handler.contentDisplay(loginAccount);
		return list;
	}
	
	public List getMembersByOrgId(long orgId,int orgType){
		return edocStatDao.getMembersByOrgId(orgId,orgType);
	}
	
	public void saveEdocStatCondition(EdocStatCondition statCondition){
	    edocStatDao.saveEdocStatCondition(statCondition);
	}
	
	public List<EdocStatCondition> getEdocStatCondition(long accountId,Map<String,Object> paramMap){
	    return edocStatDao.getEdocStatCondition(accountId,paramMap);   
	}
	
	public int getEdocStatConditionTotal(long accountId,String subject){
	    return edocStatDao.getEdocStatConditionTotal(accountId,subject); 
	}
	
	public void delEdocStatCondition(long id){
	    edocStatDao.delEdocStatCondition(id);
	}
	
	public EdocStatCondition getEdocStatConditionById(long id){
	    return edocStatDao.getEdocStatConditionById(id);
	}
	@Override
	public void delEdocStatConditionByAccountId(Long accountId) {
		edocStatDao.delEdocStatConditionByAccountId(accountId);	
	}
}
