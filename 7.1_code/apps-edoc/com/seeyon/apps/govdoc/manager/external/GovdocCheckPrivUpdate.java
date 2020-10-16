/**
 * 
 */
package com.seeyon.apps.govdoc.manager.external;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

import com.seeyon.apps.edoc.enums.EdocEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.manager.AffairManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.taglibs.functions.Functions;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.util.CheckPrivUpdate;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.manager.EdocRegisterManager;
import com.seeyon.v3x.exchange.domain.EdocRecieveRecord;
import com.seeyon.v3x.exchange.domain.EdocSendRecord;
import com.seeyon.v3x.exchange.manager.RecieveEdocManager;
import com.seeyon.v3x.exchange.manager.SendEdocManager;

/**
 * 组织模型删除收发员和部门收发员角色时校验
 * @author 唐桂林
 *
 */
public class GovdocCheckPrivUpdate implements CheckPrivUpdate {
	
	private static final Log LOGGER = CtpLogFactory.getLog(GovdocCheckPrivUpdate.class);
    
    private AffairManager affairManager;
    private SendEdocManager sendEdocManager;
    private RecieveEdocManager recieveEdocManager;    
    private EdocRegisterManager edocRegisterManager;   
    private GovdocPrivUpdateBatchTaskManagerImpl govdocPrivUpdateBatchTaskManager;

	/**
     * @param newMembers 新增的人
     * @param delMembers 删掉的人
     * @param rolename 变更的角色名称 OrgConstants.Role_NAME.Accountexchange.name()--单位收发员
     *                            OrgConstants.Role_NAME.Departmentexchange.name() --部门收发员
     * @param unitId   上一个参数对应收发员的单位ID或部门ID
     * 变更角色人员的校验：单位公文收发员、部门公文收发员
     */
    @Override
    public String processUpdate(Collection<V3xOrgMember> newMembers, Collection<V3xOrgMember> delMembers, String rolename, Long unitId) throws BusinessException  {   
    	if(!AppContext.hasPlugin("edoc")) {
    		return null;
    	}
    	String msg = null;
    	if(OrgConstants.Role_NAME.Accountexchange.name().equals(rolename) 
    	        || OrgConstants.Role_NAME.Departmentexchange.name().equals(rolename)) {//单位或部门收发员
    	    msg = exchangeDelCheck(newMembers, delMembers, rolename, unitId);
    	} else if(OrgConstants.Role_NAME.RegisterEdoc.name().equals(rolename)) {//G6登记权限
    	    msg = g6RegisterRoleCheck(newMembers, delMembers, rolename, unitId);
    	}
    	return msg;
    }
    
    /**
     * G6取消登记权限
     * @Author      : xuqiangwei
     * @Date        : 2015年1月13日下午8:01:30
     * @param newMembers
     * @param delMembers
     * @param rolename
     * @param unitId
     * @return
     * @throws BusinessException 
     */
    private String g6RegisterRoleCheck(Collection<V3xOrgMember> newMembers,
            Collection<V3xOrgMember> delMembers, String rolename, Long unitId) throws BusinessException{
        
        String ret = null;
        
        List<Long> roleDelMemberIds = getMemberIdListFromCollection(delMembers);
        //List<Long> roleNewMemberIds = getMemberIdListFromCollection(newMembers);
        
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("edocType", EdocEnum.edocType.recEdoc.ordinal());
        condition.put("orgAccountId", unitId); 
        condition.put("registerType", EdocNavigationEnum.RegisterType.All.ordinal());
        
        List<Long> warnCannotDelMemberList = new ArrayList<Long>();//不能删除的人
        
        for(Long userId : roleDelMemberIds){
            
            condition.put("userId",  userId);
            
            //待登记检查
            int waitState = EdocNavigationEnum.RegisterState.WaitRegister.ordinal();
            List<EdocRecieveRecord> list = recieveEdocManager.findWaitEdocRegisterList(waitState, condition);
            if(Strings.isNotEmpty(list)){
                //有待登记数据
                warnCannotDelMemberList.add(userId);
                continue;
            }
            
            //登记待发检查
            int draftState = EdocNavigationEnum.RegisterState.DraftBox.ordinal();
            List<EdocRegister> list2 = edocRegisterManager.findEdocRegisterList(draftState, condition);
            if(Strings.isNotEmpty(list2)){
                //有登记待发数据
                warnCannotDelMemberList.add(userId);
            }
        }
        
        if(warnCannotDelMemberList.size()>0){
            List<String> names = new ArrayList<String>();
            for(Long memberId : warnCannotDelMemberList){
                String name=Functions.showMemberNameOnly(memberId);
                if(!names.contains(name)){
                    names.add(name);
                }
                
            }
            ret = ResourceUtil.getString("edoc.alert.hasToRegistData", Functions.join(names, (PageContext)null));
        }
        
        return ret;
    }
    
    /**
     * 检查单位或部门收发员是否有未处理完的数据
     * @Author      : xuqiangwei
     * @Date        : 2015年1月13日下午7:58:17
     * @param newMembers
     * @param delMembers
     * @param rolename
     * @param unitId
     * @return
     * @throws BusinessException 
     */
    private String exchangeDelCheck(Collection<V3xOrgMember> newMembers,
            Collection<V3xOrgMember> delMembers, String rolename, Long unitId) throws BusinessException{
        
        List<Long> warnCannotDelMemberList=new ArrayList<Long>(); //最后警告不能删除的人员列表
        List<CtpAffair> copyToNewMembersAffairList=new ArrayList<CtpAffair>(); //操作验证成功后，需要给新增的人员复制的affair数据
        List<Long> delMembersAffairList=new ArrayList<Long>(); //操作验证成功后，需要把删除的人员的affair数据删除
        List<EdocSendRecord> changeAssignTypeSendRecList=new ArrayList<EdocSendRecord>(); //需要修改制定执行为竞争执行的EdocSendRecord
        List<EdocRecieveRecord> changeAssignTypeRecieveRecList=new ArrayList<EdocRecieveRecord>(); //需要修改签收人的id为0的EdocSendRecord（如果是回退的数据，由其他人接手时，需要修改为0，表示竞争执行）
        List<Long> roleDelMemberIds=getMemberIdListFromCollection(delMembers);
        List<Long> roleNewMemberIds=getMemberIdListFromCollection(newMembers);
        //先获得待交换和待签收公文objectid
        List<EdocSendRecord> sendList = sendEdocManager.getEdocSendRecordByOrgIdAndStatus(unitId, EdocSendRecord.Exchange_iStatus_Tosend);
        List<EdocRecieveRecord> recieveList = recieveEdocManager.getEdocRecieveRecordByOrgIdAndStatus(unitId, EdocRecieveRecord.Exchange_iStatus_Torecieve);

        //待发送验证
        for(EdocSendRecord sendRec: sendList){
            //获得待交换的affair数据
            List<CtpAffair> affairList = getObjectIdListAffair(sendRec.getEdocId(), ApplicationCategoryEnum.edoc.getKey(), ApplicationSubCategoryEnum.old_exSend.getKey(), null);
            if(affairList.size()==0){
                continue;
            }
            List<Long> affairMemberList = getMemberIdsListFromAffairList(affairList, null );
            List<Long> delMemberIds=new ArrayList<Long>();//affair的待发送人员，即将被删除的人员
            List<Long> oldMemberIds=new ArrayList<Long>();//affair的待发送人员，原有人员
            //affair的待发送人员分两种：即将被删除的人员和原有人员
            for(Long affairMember : affairMemberList){
                if(roleDelMemberIds.contains(affairMember)){
                    delMemberIds.add(affairMember);
                }else{
                    oldMemberIds.add(affairMember);
                }
            }
            
            if(delMemberIds.size()>0 && oldMemberIds.size()>0){ //多条affair，表明待发送数据是竞争执行的
                if(roleNewMemberIds.size()>0){//有新人接手
                    newPeopleAcceptSend(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,sendRec);
                }
            }else if(delMemberIds.size()>0 && oldMemberIds.size()==0){ 
                if(delMemberIds.size()==1){ 
                    if(roleNewMemberIds.size()>0){//有新人接手
                        newPeopleAcceptSend(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,sendRec);
                        if(sendRec.getAssignType()==EdocSendRecord.Exchange_Assign_To_Member){//指定执行要变更为竞争执行
                            changeAssignTypeSendRecList.add(sendRec);
                        }
                    }else{ //没人接手，警告
                        warnCannotDelMemberList.addAll(delMemberIds);
                        continue;
                    }
                    
                }else{//affair的人都要被删除，没有保留的原有人员
                    if(roleNewMemberIds.size()>0){//有新人接手
                        newPeopleAcceptSend(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,sendRec);
                    }else{ //没人接手，警告
                        warnCannotDelMemberList.addAll(delMemberIds);
                        continue;
                    }
                }
                
            }else if(delMemberIds.size()==0 && oldMemberIds.size()>0){
                if(roleNewMemberIds.size()>0 && sendRec.getAssignType()==EdocSendRecord.Exchange_Assign_To_All){//有新人接手
                    newPeopleAcceptSend(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,sendRec);
                }
            }else {
                continue;
            }
            
            //把要删除的人员的affair收集起来，最后删除
            if(delMemberIds.size()>0){
                for(CtpAffair c : affairList){
                    Long memberId=c.getMemberId();
                    if(delMemberIds.contains(memberId)){
                        delMembersAffairList.add(c.getId());
                    }
                }
            }
        }
        
        
        //待签收验证
        for(EdocRecieveRecord receiveRec: recieveList){
            //获得待交换的affair数据
            List<CtpAffair> affairList = getObjectIdListAffair(receiveRec.getEdocId(), ApplicationCategoryEnum.edoc.getKey(), ApplicationSubCategoryEnum.old_exSign.getKey(), null);
            if(affairList.size()==0){
                continue;
            }
            List<Long> affairMemberList = getMemberIdsListFromAffairList(affairList, receiveRec.getId()); //通过RecieveRec.getId()把其他单位或部门的affair过滤掉
            List<Long> delMemberIds=new ArrayList<Long>();//affair的待发送人员，即将被删除的人员
            List<Long> oldMemberIds=new ArrayList<Long>();//affair的待发送人员，原有人员
            //affair的待发送人员分两种：即将被删除的人员和原有人员
            for(Long affairMember : affairMemberList){
                if(roleDelMemberIds.contains(affairMember)){
                    delMemberIds.add(affairMember);
                }else{
                    oldMemberIds.add(affairMember);
                }
            }
            
            if(delMemberIds.size()>0 && oldMemberIds.size()>0){ //多条affair，表明待发送数据是竞争执行的
                if(roleNewMemberIds.size()>0){
                    newPeopleAcceptReceive(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,receiveRec);
                    if(receiveRec.getRecUserId()!=0L){
                        changeAssignTypeRecieveRecList.add(receiveRec);
                    }
                }
            }else if(delMemberIds.size()>0 && oldMemberIds.size()==0){ 
                if(delMemberIds.size()==1){ 
                    //竞争执行
                    if(roleNewMemberIds.size()>0){//有新人接手
                        newPeopleAcceptReceive(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,receiveRec);
                        if(receiveRec.getRecUserId()!=0L){
                            changeAssignTypeRecieveRecList.add(receiveRec);
                        }
                    }else{ //没人接手，警告
                        warnCannotDelMemberList.addAll(delMemberIds);
                        continue;
                    }
                }else{//affair的人都要被删除，没有保留的原有人员
                    if(roleNewMemberIds.size()>0){//有新人接手
                        newPeopleAcceptReceive(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,receiveRec);
                        if(receiveRec.getRecUserId()!=0L){
                            changeAssignTypeRecieveRecList.add(receiveRec);
                        }
                    }else{ //没人接手，警告
                        warnCannotDelMemberList.addAll(delMemberIds);
                        continue;
                    }
                }
                
            }else if(delMemberIds.size()==0 && oldMemberIds.size()>0){
                if(roleNewMemberIds.size()>0){//有新人接手
                    newPeopleAcceptReceive(roleNewMemberIds, affairList.get(0), copyToNewMembersAffairList,receiveRec);
                    if(receiveRec.getRecUserId()!=0L){
                        changeAssignTypeRecieveRecList.add(receiveRec);
                    }
                }
            }else {
                continue;
            }
            
            //把要删除的人员的affair收集起来，最后删除
            if(delMemberIds.size()>0){
                for(CtpAffair c : affairList){
                    Long memberId=c.getMemberId();
                    if(delMemberIds.contains(memberId)){
                        delMembersAffairList.add(c.getId());
                    }
                }
            }
        }
        
        if(warnCannotDelMemberList.size()>0){
            List<String> names = new ArrayList<String>();
            for(Long memberId : warnCannotDelMemberList){
                String name=Functions.showMemberNameOnly(memberId);
                if(!names.contains(name)){
                    names.add(name);
                }
                
            }
            return ResourceUtil.getString("edoc.alert_hasExchangePendingAffair", Functions.join(names, (PageContext)null));
        }
        //给新增的人批量插入affair
        if(copyToNewMembersAffairList.size()>0){
            affairManager.saveAffairs(copyToNewMembersAffairList);
        }
        
        //删除的人员待交换数据物理删除
        Map<String, Object> taskMap = new HashMap<String, Object>();
        taskMap.put("deleteAffairIds", delMembersAffairList);
        govdocPrivUpdateBatchTaskManager.addTask(taskMap);
        
         //更新指定执行为竞争执行
        if(changeAssignTypeSendRecList.size()>0){
            for(EdocSendRecord c : changeAssignTypeSendRecList){
                c.setAssignType(EdocSendRecord.Exchange_Assign_To_All);
                c.setSendUserId(0L);
                sendEdocManager.update(c);
            }
        }
        
        //更改待签收的数据的签收人id为0（如果是回退的数据，rec_user_id是指定的用户，由其他人接手时，需要修改为0，表示竞争执行）
        if(changeAssignTypeRecieveRecList.size()>0){
            for(EdocRecieveRecord e : changeAssignTypeRecieveRecList){
                e.setRecUserId(0L);
                recieveEdocManager.update(e);
            }
        }

        return null;
    }
    
    private List<CtpAffair> getObjectIdListAffair(Long objectId, int  app, int subApp, Long memberId) throws BusinessException{
    	//待发送、待签收的数据
		Map<String, Object> conditions = new HashMap<String, Object>();
		if(objectId!=null){
			conditions.put("objectId", objectId);
		}
		if(memberId!=null){
			conditions.put("memberId", memberId);
		}
		conditions.put("state",StateEnum.col_pending.getKey());
		conditions.put("app", app);
		conditions.put("subApp", subApp);
		conditions.put("delete", false);
		List<CtpAffair> list=affairManager.getByConditions(null, conditions);
		return list;
    }
    
    
    private List<Long> getMemberIdListFromCollection(Collection<V3xOrgMember> members){
    	List<Long> list = new ArrayList<Long>();
    	if(members!=null){
    	    for(V3xOrgMember m:members){
    	    	if(m.isValid()){
    	    		list.add(m.getId());
    	    	}
    	    	
    	    }
    	}

	    return list;
    }
    
    private List<Long> getMemberIdsListFromAffairList(List<CtpAffair> affairList, Long recieveRecordId){
    	List<Long> affairMemberList = new ArrayList<Long>();
		for(CtpAffair c : affairList){
			//如果待签收的affair中subObjectId等于recieveRecord的Id，表明签收的数据是这个单位的，才能有效。否则就是发给其他单位的
			//-场景：一个公文发送给多个单位，只查affair会把其他单位的查出来,引起判断错误
			if(recieveRecordId!=null && c.getSubObjectId()!=null && recieveRecordId.longValue()!=c.getSubObjectId().longValue()){
				continue;
			}
			if(!affairMemberList.contains(c.getMemberId())){
				affairMemberList.add(c.getMemberId());
			}
		}
		return affairMemberList;
    }
    //待签收
    private void newPeopleAcceptReceive(List<Long> roleNewMemberIds,CtpAffair ctpAffair, List<CtpAffair> copyToNewMembersAffairList,EdocRecieveRecord receiveRec){
    	newPeopleAccept(roleNewMemberIds,ctpAffair,copyToNewMembersAffairList,receiveRec.getId());
    }
    //待发送
    private void newPeopleAcceptSend(List<Long> roleNewMemberIds,CtpAffair ctpAffair, List<CtpAffair> copyToNewMembersAffairList,EdocSendRecord sendRec){
    	newPeopleAccept(roleNewMemberIds,ctpAffair,copyToNewMembersAffairList,sendRec.getId());
    }
  //有新人接手
    private void newPeopleAccept(List<Long> roleNewMemberIds,CtpAffair ctpAffair, List<CtpAffair> copyToNewMembersAffairList,Long subObjectId){
		for(Long roleNewMemberId : roleNewMemberIds){
			CtpAffair c = new CtpAffair();
			try {
				c=(CtpAffair)ctpAffair.clone();
				c.setNewId();
				c.setMemberId(roleNewMemberId);
				c.setSubObjectId(subObjectId);
				copyToNewMembersAffairList.add(c);
			} catch (CloneNotSupportedException e) {
				LOGGER.error("变更公文收发员克隆数据异常：",e);
			}
		}
		
    }

	public void setAffairManager(AffairManager affairManager) {
        this.affairManager = affairManager;
    }
    public void setSendEdocManager(SendEdocManager sendEdocManager) {
		this.sendEdocManager = sendEdocManager;
	}
	public void setRecieveEdocManager(RecieveEdocManager recieveEdocManager) {
		this.recieveEdocManager = recieveEdocManager;
	}
	public void setEdocRegisterManager(EdocRegisterManager edocRegisterManager) {
        this.edocRegisterManager = edocRegisterManager;
    }
	public void setGovdocPrivUpdateBatchTaskManager(GovdocPrivUpdateBatchTaskManagerImpl govdocPrivUpdateBatchTaskManager) {
		this.govdocPrivUpdateBatchTaskManager = govdocPrivUpdateBatchTaskManager;
	}

}
