package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.seeyon.ctp.common.ModuleType;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.template.CtpTemplateCategory;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.memberleave.bo.MemberLeaveDetail;
import com.seeyon.ctp.organization.memberleave.manager.AbstractMemberLeaveClearItem;
import com.seeyon.ctp.util.UniqueList;

/**
 * 
 * 离职办理：協同模板
 * 
 * @author tanmf
 *
 */
public class MemberLeaveClearItemInterfaceColImpl extends AbstractMemberLeaveClearItem  {
	private final static Log     logger       = CtpLogFactory.getLog(MemberLeaveClearItemInterfaceColImpl.class);
    private TemplateManager templateManager;
    private OrgManager orgManager;
    
    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }
    
    public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	@Override
    public List<MemberLeaveDetail> getItems(long memberId) throws BusinessException {
        List<MemberLeaveDetail> result = new ArrayList<MemberLeaveDetail>();
        getTemplateCategoryItems(result,memberId,ResourceUtil.getString("member.leave.collaborationtemplatemanager.title"));
        return result;
    }
    
    public List<MemberLeaveDetail> getTemplateCategoryItems(List<MemberLeaveDetail> result,long memberId,String content) throws BusinessException {
    	MemberLeaveDetail memberLeaveDetail = null;
    	Map<Long,String> map =new HashMap<Long,String>();
    	List<CtpTemplateCategory> templateCategorys = templateManager.getAllTemplateCategoryListByMemberId(memberId,ModuleType.collaboration);
        String accountName="";
        
        for (CtpTemplateCategory templateCategory : templateCategorys) {
			if(map.containsKey(templateCategory.getOrgAccountId())){
				accountName=map.get(templateCategory.getOrgAccountId());
			}else{
				Long accountId=templateCategory.getOrgAccountId();
				V3xOrgAccount v3xOrgAccount = orgManager.getAccountById(accountId);
				accountName=v3xOrgAccount.getName();
				map.put(accountId, accountName);
			}
			memberLeaveDetail= new MemberLeaveDetail();
			memberLeaveDetail.setId(templateCategory.getId().toString());
			memberLeaveDetail.setDealInterfaceClassName(this.getClass().getSimpleName());
			memberLeaveDetail.setType(ResourceUtil.getString("member.leave.collaboration.title"));
			memberLeaveDetail.setAccountName(accountName);
			memberLeaveDetail.setContent(content);
			memberLeaveDetail.setTitle(templateCategory.getName());
			result.add(memberLeaveDetail);
		}
        
        return result;
    }
    

    @Override
    public Integer getSortId() {
        return 15;
    }

    
	@Override
	public Category getCategory() {
		return Category.Manager;
	}

	@Override
	public void updateAuthority(Long oldMemberId, Long newMemberId,List<String> authIds) throws BusinessException {
		V3xOrgMember oldMember = orgManager.getMemberById(oldMemberId);
		if(oldMember == null){
			return;
		}
		String authNames = getAuthNames(oldMemberId,authIds,"title","content");
		
		List<Long> categoryIds = new UniqueList<Long>();
		for(String id : authIds){
			categoryIds.add(Long.valueOf(id));
		}
		
		try {
			templateManager.replaceTemplateAuth(categoryIds, oldMemberId, newMemberId);
		} catch (Exception e) {
			logger.error("替换模板分类管理员异常!",e);
		}
		
		//记录交接日志
		saveLog(oldMemberId,newMemberId,authNames);
	}

}
