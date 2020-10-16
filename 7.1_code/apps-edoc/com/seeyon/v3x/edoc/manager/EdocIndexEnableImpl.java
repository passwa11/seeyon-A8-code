package com.seeyon.v3x.edoc.manager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.apps.index.bo.AuthorizationInfo;
import com.seeyon.apps.index.bo.IndexInfo;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.filemanager.manager.PartitionManager;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.po.filemanager.Partition;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocOpinion;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class EdocIndexEnableImpl extends EdocManagerImpl {
	private final static Log log = LogFactory.getLog(EdocIndexEnableImpl.class);

    protected PartitionManager partitionManager;
    protected IndexApi indexApi;

	public void setPartitionManager(PartitionManager partitionManager) {
		this.partitionManager = partitionManager;
	}
	
	public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }

	public Integer findIndexResumeCount_old(Date arg0, Date arg1)
			throws BusinessException { 
		   StringBuilder sbHql = new StringBuilder("select count(b.id) from EdocSummary ");
	        sbHql.append(" as b where b.state in (0,1,3) and b.createTime >= ? and b.createTime <= ? ");
	        Number result = 0;
	       result = (Number)edocSummaryDao.findUnique(sbHql.toString(), null, arg0,arg1);
	        return result==null?0:result.intValue();
	}

	public List<Long> findIndexResumeIDList_old(Date arg0, Date arg1, Integer arg2,Integer arg3) throws BusinessException {
		return edocSummaryDao.findIndexResumeIDList(arg0, arg1, arg2, arg3);
	}

 
	private boolean isEdocIndexenable(Integer app){
		return Integer.valueOf(ApplicationCategoryEnum.edocSend.getKey()).equals(app)
			|| Integer.valueOf(ApplicationCategoryEnum.edocRec.getKey()).equals(app)
			|| Integer.valueOf(ApplicationCategoryEnum.edocSign.getKey()).equals(app)
			|| Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(app);
	}
	
	public Map<String, Object> findSourceInfo_old(Long summaryId) throws BusinessException {
		Map<String, Object> parmas = new HashMap<String, Object>();
		Long affairId = 0l;
		List<StateEnum> state = new ArrayList<StateEnum>();
		state.add(StateEnum.col_sent);
		state.add(StateEnum.col_pending);
		state.add(StateEnum.col_done);
		state.add(StateEnum.col_stepStop);
		state.add(StateEnum.col_waitSend);
        List<CtpAffair> affairs = this.affairManager.getAffairs(summaryId, state);
		if (Strings.isNotEmpty(affairs)){
			Long userId = 0l;
			User user = AppContext.getCurrentUser();
			if(user != null){
				userId = user.getId();
			}
			if(Long.valueOf(0l).equals(userId)){
				affairId = ((CtpAffair) affairs.get(0)).getId();
			}else{
				for(CtpAffair affair : affairs){
						
					if(affair.getMemberId().equals(userId) && isEdocIndexenable(affair.getApp()) && AffairUtil.isAfffairValid(affair, true)){
						affairId = affair.getId();
						break;
					}
				}
			}
		}
			
		parmas.put("sourceId",affairId);
		return parmas;
	}

	public Integer getAppEnumKey() {
		return ApplicationCategoryEnum.edoc.getKey();
	}
	public IndexInfo getIndexInfo_old(EdocSummary summary) throws BusinessException {
		Long id = summary.getId();
		
		IndexInfo info = new IndexInfo();
		
		info.setEntityID(id);
		info.setAppType(ApplicationCategoryEnum.edoc);
		

		//在此取得权限！！
		AuthorizationInfo ai = new AuthorizationInfo();
		List<Long> list = affairManager.getAffairMemberIds(ApplicationCategoryEnum.edoc, id);
		ai.setOwner(list);
		info.setAuthorizationInfo(ai);
	
		V3xOrgMember member = null;
		Long memberId  = 0l;
		String memberName = "";
        try {
            member = orgManager.getEntityById(V3xOrgMember.class, summary.getStartUserId());
            if(member != null){
            	memberId = member.getId();
            	memberName = member.getName();
            }else{
            	log.info("getIndexInfo is null,memberid:"+summary.getStartUserId());
            }
        }
        catch (BusinessException e) {
        	log.error("ColManagerImpl getIndexInfo getColAllById", e);
            throw new BusinessException("ColManagerImpl getIndexInfo getEntityById", e);
        }

		info.setStartMemberId(memberId);
		info.setAuthor(memberName);
		info.setTitle(summary.getSubject());
		java.util.Date date1 = new java.util.Date(summary.getCreateTime().getTime());
		info.setCreateDate(date1);
		Set<EdocOpinion> opinions = summary.getEdocOpinions();
//		StringBuffer commentStr = null;
		StringBuilder opinionStr = null;

		if (opinions != null && opinions.size() > 0) {
			opinionStr = new StringBuilder();
			Iterator<EdocOpinion> it1 = opinions.iterator();
			while (it1.hasNext()) {
				EdocOpinion opin = (EdocOpinion) it1.next();
				if (opin.getContent() != null && opin.getIsHidden() == false){
					Long userId = opin.getCreateUserId();
					String userName= "*";
					  try {
						     member = orgManager.getMemberById(userId);
				            userName = member.getName();
					  }
			        catch (BusinessException e) {
			        	log.error("edoc getIndexInfo",e);
			        }
			        String attitude = "";//态度
			    	if (opin.getAttribute() > 0) {
						attitude = enumManagerNew.getEnumItemLabel(
								EnumNameEnum.collaboration_attitude, Integer
										.toString(opin.getAttribute()));
					}
					if (Strings.isNotBlank(attitude)) {
							attitude = ResourceUtil.getString(attitude); 
					} else if (Strings.isNotBlank(attitude)&& Integer.valueOf(attitude).intValue() == com.seeyon.v3x.edoc.util.Constants.EDOC_ATTITUDE_NULL) {
						   attitude = "";
					}
			        //去掉处理态度
					//opinionStr.append(userName+" "+ attitude+" "+opin.getContent());
					opinionStr.append(userName+" "+opin.getContent());
				}
			}
			info.setOpinion(opinionStr.toString());
		}
		EdocBody body = summary.getFirstBody();
		if(body == null) {
			log.error("**** 检索到该老公文正文为空！！！ " + summary.getId());
			return null;
		}
		
		boolean isWord =  "OfficeWord".equals(body.getContentType());
		boolean isExecl =  "OfficeExcel".equals(body.getContentType());
		boolean isWps=  "WpsWord".equals(body.getContentType());
		boolean isWpsExcel =  "WpsExcel".equals(body.getContentType());
		boolean isPDF =  "Pdf".equals(body.getContentType());
		
		if ("HTML".equals(body.getContentType())) {
			info.setContentType(IndexInfo.CONTENTTYPE_HTMLSTR);
			info.setContent(Strings.isBlank(body.getContent())?"":body.getContent());
		} 
		else if(isPDF || isWpsExcel || isWps || isExecl || isWord) {
			
			if(isWord){
				info.setContentType(IndexInfo.CONTENTTYPE_WORD);
			}
			else if(isExecl){
				info.setContentType(IndexInfo.CONTENTTYPE_XLS);
			}
			else if(isWps){
				info.setContentType(IndexInfo.CONTENTTYPE_WPS_Word);
			}
			else if(isWpsExcel){
				info.setContentType(IndexInfo.CONTENTTYPE_WPS_EXCEL);
			}
			else if(isPDF){
				info.setContentType(IndexInfo.CONTENTTYPE_PDF);
			}
			if(Strings.isBlank(body.getContent() )) {
				log.error("**** 检索到该老公文正文ID为空！！！ " + summary.getId());
				return null;
			}
			
			Long fileId = Long.parseLong(body.getContent());
			Date date = new Date(body.getCreateTime().getTime());
			info.setContentID(fileId);
			info.setContentCreateDate(date);
			Partition partition = partitionManager.getPartition(date, true);
			info.setContentAreaId(partition.getId().toString());
			String contentPath = this.fileManager.getFolder(body.getCreateTime(), false);
			info.setContentPath(contentPath.substring(contentPath.length()-11)+System.getProperty("file.separator"));
		}
		
		//在此处理附件
		indexApi.convertToAccessory(info);
		//}
		info.setHasAttachment(summary.isHasAttachments());
		info.setImportantLevel(summary.getImportantLevel());
		int t =  IndexInfo.FieldIndex_Type.IndexTOKENIZED.ordinal();
		
        StringBuilder  keyword=new StringBuilder();
        keyword.append(summary.getSecretLevel()==null?"":summary.getSecretLevel()+" ");
        keyword.append(String.valueOf(summary.getEdocType())+" ");
        keyword.append(StringUtils.isBlank(summary.getDocMark())?"":summary.getDocMark()+" ");
        keyword.append(StringUtils.isBlank(summary.getSerialNo())?"":summary.getSerialNo()+" ");
        keyword.append(StringUtils.isBlank(summary.getKeywords())?"":summary.getKeywords()+" ");
        keyword.append(StringUtils.isBlank(summary.getSendUnit())?"":summary.getSendUnit()+" ");
        keyword.append(StringUtils.isBlank(summary.getIssuer())?"":summary.getIssuer()+" ");
        keyword.append(StringUtils.isBlank(summary.getSendTo())?"":summary.getSendTo()+" ");
        keyword.append(StringUtils.isBlank(summary.getCopyTo())?"":summary.getCopyTo()+" ");
        keyword.append(StringUtils.isBlank(summary.getReportTo())?"":summary.getReportTo()+" ");
        keyword.append(StringUtils.isBlank(summary.getSendType())?"":summary.getSendType()+" ");
        
		info.addExtendProperties(IndexInfo.SECRETLEVEL,summary.getSecretLevel(),t);
		info.addExtendProperties(IndexInfo.EDOCTYPE,String.valueOf(summary.getEdocType()),t);
		info.addExtendProperties(IndexInfo.SENDTYPE,summary.getSendType(),t);
		info.addExtendProperties(IndexInfo.DOCMARK,summary.getDocMark(),t);
		info.addExtendProperties(IndexInfo.SERIALNO,summary.getSerialNo(),t);
		info.addExtendProperties(IndexInfo.EDOCKEYWORD,summary.getKeywords(),t);
		info.addExtendProperties(IndexInfo.SENDUNIT,summary.getSendUnit(),t);
		info.addExtendProperties(IndexInfo.ISSUER,summary.getIssuer(),t);
		info.addExtendProperties(IndexInfo.SENDTO,summary.getSendTo(),t);
		info.addExtendProperties(IndexInfo.COPYTO,summary.getCopyTo(),t);
		info.addExtendProperties(IndexInfo.REPORTTO,summary.getReportTo(),t);
		if(summary.getSigningDate()!=null)
			info.addExtendProperties("signingDate", String.valueOf(summary.getSigningDate()), t);
		
		//组合自定义元素意见
		StringBuilder edocCustomInfo = new StringBuilder();
		try{
			Method[] m = summary.getClass().getDeclaredMethods();
			for(Method method : m){
				if(method.getName().indexOf("getString")!=-1 ||
						method.getName().indexOf("getText")!=-1||
						method.getName().indexOf("getInteger")!=-1||
						method.getName().indexOf("getDecimal")!=-1||
						method.getName().indexOf("getDate")!=-1||
						method.getName().indexOf("getList")!=-1){
				
					Object o= method.invoke(summary);
					if(o==null){continue;}
					String tem=String.valueOf(o);
					if(StringUtils.isNotBlank(tem))
					{
						edocCustomInfo.append(tem+" ");
					}
				}
			}
			info.addExtendProperties("edocCustomInfo", edocCustomInfo.toString(), t);
			
		}catch(Exception e){
			log.error("edoc getIndexinfo 获取自定义公文元素值反射异常",e);
		}
		//签发日期，SendType,edocCustomInfo ,关联文档名称
		//info.addExtendProperties(IndexInfo.SENDTYPE, value, fieldIndexType)
		info.setKeyword(keyword.append(" "+edocCustomInfo.toString()).toString());
		return info;
	}

    public boolean isShowIndexSummary_old(Long id, Map<String, String> extendProperties) throws BusinessException {
        return true;
    }

}
