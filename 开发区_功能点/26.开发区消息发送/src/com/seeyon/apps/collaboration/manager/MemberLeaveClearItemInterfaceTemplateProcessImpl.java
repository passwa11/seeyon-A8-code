package com.seeyon.apps.collaboration.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.template.manager.TemplateManager;
import com.seeyon.ctp.common.template.vo.CtpTemplateVO;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.organization.memberleave.bo.MemberLeaveDetail;
import com.seeyon.ctp.organization.memberleave.manager.MemberLeaveClearItemInterface;
import com.seeyon.ctp.workflow.wapi.WorkflowApiManager;
import com.seeyon.v3x.common.web.login.CurrentUser;

/**
 * 
 * 离职办理：显示流程节点包含离职人员的模板名称
 * 
 * @author tanmf
 *
 */
public class MemberLeaveClearItemInterfaceTemplateProcessImpl implements MemberLeaveClearItemInterface {
    
    private TemplateManager templateManager;
    private OrgManager orgManager;
    private WorkflowApiManager wapi;
    
    public WorkflowApiManager getWapi() {
		return wapi;
	}

	public void setWapi(WorkflowApiManager wapi) {
		this.wapi = wapi;
	}

	public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }
    
    public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	@Override
    public List<MemberLeaveDetail> getItems(long memberId) throws BusinessException {
        List<MemberLeaveDetail> result = new ArrayList<MemberLeaveDetail>();
        getTemplateListItems(result,memberId,ResourceUtil.getString("member.leave.processnode.title"));
        
        return result;
    }
    
	//模板流程	流程节点	模板名称（包含协同、公文和表单流程）
    private List<MemberLeaveDetail> getTemplateListItems(List<MemberLeaveDetail> result,long memberId,String content) throws BusinessException {
    	User user= CurrentUser.get();
    	MemberLeaveDetail memberLeaveDetail = null;
    	Map<Long,String> map =new HashMap<Long,String>();
        //List<CtpTemplate> templates = templateManager.getTemplateByWorflowUserId(memberId);

    	List<CtpTemplateVO> templates= wapi.getCtpTemplateByOrgIdsAndCategory(user.getId(), null, String.valueOf(memberId), null);
        String accountName="";
        
        for (CtpTemplateVO ctpTemplate : templates) {
			if(map.containsKey(ctpTemplate.getOrgAccountId())){
				accountName=map.get(ctpTemplate.getOrgAccountId());
			}else{
				Long accountId=ctpTemplate.getOrgAccountId();
				V3xOrgAccount v3xOrgAccount = orgManager.getAccountById(accountId);
				accountName=v3xOrgAccount.getName();
				map.put(accountId, accountName);
			}
			memberLeaveDetail= new MemberLeaveDetail();
			memberLeaveDetail.setType(ResourceUtil.getString("member.leave.templateprocess.title"));
			memberLeaveDetail.setAccountName(accountName);
			memberLeaveDetail.setContent(content);
			memberLeaveDetail.setTitle(ctpTemplate.getSubject());
			result.add(memberLeaveDetail);
		}
        
        return result;
    }    

    @Override
    public Integer getSortId() {
        return 5;
    }

	@Override
	public Category getCategory() {
		return Category.process;
	}

	public String getItemInterfaceName() throws BusinessException{
		return this.getClass().getSimpleName();
	}

	public void updateAuthority(Long oldMemberId, Long newMemberId, List<String> authIds) throws BusinessException{

	}

	public void updateAuthority2(Long oldMemberId, Long newMemberId, List<String> authIds) throws BusinessException{
	}
}
