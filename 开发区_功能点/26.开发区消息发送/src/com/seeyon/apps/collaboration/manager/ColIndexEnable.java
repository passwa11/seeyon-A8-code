/**
 * $Author$
 * $Rev$
 * $Date::                     $:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */

package com.seeyon.apps.collaboration.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.apps.collaboration.api.CollaborationApi;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.collaboration.util.ColUtil;
import com.seeyon.apps.index.api.IndexApi;
import com.seeyon.apps.index.bo.AuthorizationInfo;
import com.seeyon.apps.index.bo.IndexInfo;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceBundleUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.parser.StrExtractor;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;

/**
 * 协同全文检索
 * @author wulin
 *
 */
public class ColIndexEnable  {
	private static Log LOG = CtpLogFactory.getLog(ColIndexEnable.class);
    
    private CommentManager ctpCommentManager ;
    private AffairManager affairManager;
    private OrgManager orgManager;
    private IndexApi indexApi;
    private CollaborationApi collaborationApi = (CollaborationApi)AppContext.getBean("collaborationApi");
    
    public void setIndexApi(IndexApi indexApi) {
        this.indexApi = indexApi;
    }
    
    /**
     * @param ctpCommentManager the ctpCommentManager to set
     */
    public void setCtpCommentManager(CommentManager ctpCommentManager) {
        this.ctpCommentManager = ctpCommentManager;
    }

    /**
     * @param affairManager the affairManager to set
     */
    public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }

    /**
     * @param orgManager the orgManager to set
     */
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public Integer findIndexResumeCount(Date beginDate, Date endDate) throws BusinessException {
        return this.collaborationApi.getColSummaryCount(beginDate, endDate,false);
    }

  
    public List<Long> findIndexResumeIDList(Date starDate, Date endDate, Integer firstRow, Integer pageSize) throws BusinessException {
        return this.collaborationApi.findColSummaryIdList(starDate, endDate, firstRow, pageSize,false);
    }

   
    @SuppressWarnings("unchecked")
    public Map<String, Object> findSourceInfo(Long summaryId) throws BusinessException {
		Map parmas = new HashMap();
		Long affairId = 0l;
		List<CtpAffair> affairs = this.affairManager.getAffairs(ApplicationCategoryEnum.collaboration, summaryId.longValue());
		if(Strings.isEmpty(affairs)){
		  affairs = this.affairManager.getAffairsHis(ApplicationCategoryEnum.collaboration, summaryId.longValue());
		}
		if (Strings.isNotEmpty(affairs)){
			Long userId = 0l;
			User user = AppContext.getCurrentUser();
			if(user != null){
				userId = user.getId();
			}
			if(Long.valueOf(0l).equals(userId)){
				for(CtpAffair affair : affairs){
					if(ColUtil.isAfffairValid(affair, false)){
						affairId = affair.getId();
						break;
					}
				}
			}else{
				for(CtpAffair affair : affairs){
					if(affair.getMemberId().equals(userId) && ColUtil.isAfffairValid(affair, false)){
						affairId = affair.getId();
						break;
					}
				}
			}
		}
			
		parmas.put("sourceId",affairId);
		return parmas;
    }


    public IndexInfo getIndexInfo(Long id) throws BusinessException {
    	if(id == null){
			LOG.error("协同全文检索入库异常，传入参数为null");
			throw new BusinessException(new NullPointerException());
		}
        ColSummary colSummary = collaborationApi.getColSummary(id);
        if (colSummary == null) {
            CtpAffair affair = this.affairManager.getSimpleAffair(id);
            if (affair != null) {
                colSummary =  collaborationApi.getColSummary(affair.getObjectId());
            } else {
                return null;
            }
            if (colSummary == null) {
                return null;
            }
        }
        
        AppContext.putThreadContext("IndexInfo_summary", colSummary);
        
        IndexInfo info = new IndexInfo();
        //V320增加项 start 
        info.setStartMemberId(colSummary.getStartMemberId());
        info.setHasAttachment(ColUtil.isHasAttachments(colSummary));
        info.setImportantLevel(colSummary.getImportantLevel());
        //end
        
        
        info.setEntityID(id);
        if(ColUtil.isForm(colSummary.getBodyType())){
            info.setAppType(ApplicationCategoryEnum.form);
        }else{
            info.setAppType(ApplicationCategoryEnum.collaboration);
        }
    
        //在此取得权限！！
        AuthorizationInfo ai = new AuthorizationInfo();
        List<Long> list = affairManager.getAffairMemberIds(ApplicationCategoryEnum.collaboration, id);
        ai.setOwner(list);
        info.setAuthorizationInfo(ai);
        
        V3xOrgMember member = null;
        try {
            member = orgManager.getEntityById(V3xOrgMember.class, colSummary.getStartMemberId());
        }
        catch (BusinessException e) {
            LOG.error("ColManagerImpl getIndexInfo getColAllById", e);
            throw new BusinessException("ColManagerImpl getIndexInfo getEntityById", e);
        }
        
        info.setAuthor(member == null ? "" : member.getName());
        info.setTitle(colSummary.getSubject());
        Date date1 = new Date(colSummary.getCreateDate().getTime());
        info.setCreateDate(date1);
        
        //意见
        List<Comment> comments = ctpCommentManager.getCommentAllByModuleId(ModuleType.collaboration, id);
        if (comments != null && comments.size() > 0) {
            StringBuilder commentStr = new StringBuilder();
            for (Comment comm : comments) {
                String content = comm.getContent();
                if (Boolean.FALSE.equals(comm.isHidden()) && Strings.isNotBlank(content)) {
                    String c = null;
                    
                    if(content.contains("<")){ //做一个简单的性能提升改进
                        c = StrExtractor.getHTMLContent(content);
                    }
                    else{
                        c = Strings.toText(content);
                    }
                    
                    if(Strings.isNotBlank(c)){
                        appendWriteName(commentStr, comm.getExtAtt2() ,comm.getCreateId());
                        
                        commentStr.append(":").append(c).append(" ");
                    }
                }
            }
            
            info.setComment(commentStr.toString());
        }
        
        //在此处理附件
        indexApi.convertToAccessory(info);
        
        return info;
    }
    
    
    private void appendWriteName(StringBuilder opinionStr, String proxyName,Long writeMemberId) {
        if(writeMemberId != null){
            opinionStr.append(Functions.showMemberNameOnly(writeMemberId));
        }
        if(Strings.isNotBlank(proxyName)){
            opinionStr.append(ResourceBundleUtil.getString("com.seeyon.v3x.collaboration.resources.i18n.CollaborationResource","col.opinion.proxy",proxyName));
        }
    }
    
}
