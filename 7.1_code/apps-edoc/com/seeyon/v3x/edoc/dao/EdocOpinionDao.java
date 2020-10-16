package com.seeyon.v3x.edoc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.constants.EdocOpinionDisplayEnum.OpinionDisplaySetEnum;
import com.seeyon.v3x.edoc.domain.EdocOpinion;

public class EdocOpinionDao extends BaseHibernateDao<EdocOpinion> {

    
    private static final Log log = LogFactory.getLog(EdocOpinionDao.class);
    
    /**
     * 根据排序读取最终意见，不要改变读取排列顺序
     * @param summaryId
     * @return
     */
    /*
    public List<EdocOpinion> findEdocOpinionBySummaryId(long summaryId)
    {
        String hsql="from EdocOpinion as eo where eo.edocSummary.id=? order by eo.createTime desc,eo.nodeId,eo.createUserId";
        Object[]values={new Long(summaryId)};
        List<EdocOpinion> ls=super.find(hsql, values);
        return ls;
    }
    */
    /**
     * 根据排序读取最终意见
     * @param summaryId
     * @param timeSort 排序规则
     * @return
     */
    public List<EdocOpinion>findEdocOpinionBySummaryId(long summaryId,boolean timeSort){
        String hsql = "";
        if(timeSort) {
            hsql ="from EdocOpinion as eo where eo.edocSummary.id=? order by eo.createTime desc,eo.nodeId,eo.createUserId";
        }else {
            hsql="from EdocOpinion as eo where eo.edocSummary.id=? order by eo.createTime asc,eo.nodeId,eo.createUserId";
        }       
        Object[]values={Long.valueOf(summaryId)};
        List<EdocOpinion> ls=super.findVarargs(hsql, values);
        return ls;
    }
    
    /**
     * 只读取每个节点每个人的最后处理意见，回退处理多次，只取最后一次
     * @param summaryId
     * @return
     */
    public List<EdocOpinion> findLastEdocOpinionBySummaryId(long summaryId,boolean timeSort)
    {
        List<EdocOpinion> ls=findEdocOpinionBySummaryId(summaryId,timeSort);
        List<EdocOpinion> nls=new ArrayList<EdocOpinion>();
        Hashtable <String,EdocOpinion> hs=new Hashtable <String,EdocOpinion>();
        String key;
        for(EdocOpinion eo:ls)
        {
            //兼容历史数据，历史数据没法判断最终处理，要全部显示出来；发起人附言全部显示出来
            if(eo.getNodeId()==-1 || eo.getOpinionType()==EdocOpinion.OpinionType.senderOpinion.ordinal())
            {
                nls.add(eo);
                continue;
            }
            key=Long.toString(eo.getNodeId())+eo.getCreateUserId();
            if(hs.get(key)!=null)
            {
                //排序改变之后,增加日期比较判断
                if(hs.get(key).getCreateTime().after(eo.getCreateTime())){continue;}
                nls.remove(hs.get(key));            
            }       
            nls.add(eo);
            hs.put(key,eo);
        }
        return nls;
    }
    
    public EdocOpinion findBySummaryIdAndAffairId(long summaryId,long affairId)
    {
        EdocOpinion eo=null;
        String hsql="from EdocOpinion as eo where eo.edocSummary.id=? and eo.affairId=? and eo.state!=1 order by eo.createTime";
        Object[]values={Long.valueOf(summaryId), Long.valueOf(affairId)};
        List<EdocOpinion> ls=super.findVarargs(hsql, values);
        if(ls!=null && ls.size()>0){eo=ls.get(ls.size()-1);}
        return eo;      
    }
    
    /**
     * 删除发起人附言
     * 
     * @param summaryId
     */
    public void deleteSenderOpinion(Long summaryId)
    {       
        String hsql="delete from EdocOpinion as eo where eo.edocSummary.id=? and eo.opinionType = "+EdocOpinion.OpinionType.senderOpinion.ordinal();
        super.bulkUpdate(hsql, null, summaryId);
    }

    /**
     * 删除处理意见，不包括发起人意见,和getDealOpinion联系紧密，修改时请注意影响
     * 
     * @param summaryId
     */
    public void deleteDealOpinion(Long summaryId)
    {       
        String hsql="delete from EdocOpinion as eo where eo.edocSummary.id=? and eo.opinionType<>"+EdocOpinion.OpinionType.senderOpinion.ordinal();
        //String hsql="from EdocOpinion as eo where eo.edocSummary.id="+summaryId+" and eo.opinionType<>"+EdocOpinion.OpinionType.senderOpinion.ordinal();
        super.bulkUpdate(hsql, null, summaryId);
    }
    
    /**
     * 删除处理意见，不包括发起人意见,和getDealOpinion联系紧密，修改时请注意影响
     * 
     * @param SUMMARYID
     */
    public void deleteDealOpinionByAffairId(Long affairId)
    {       
        String hsql="delete from EdocOpinion as eo where eo.affairId=?";
        super.bulkUpdate(hsql, null, affairId);
    }
   
    /**
     * 获取处理意见，不包括发起人意见， 和deleteDealOpinion联系紧密，修改时请注意影响
     * 
     * @param summaryId
     */
    public List<EdocOpinion> getDealOpinion(Long summaryId){
        String findSql = "from EdocOpinion as eo where eo.edocSummary.id=? and eo.opinionType<>"+EdocOpinion.OpinionType.senderOpinion.ordinal();
        Object[] values = {summaryId};
        List<EdocOpinion> ls=super.findVarargs(findSql, values);
        if(Strings.isEmpty(ls)){
            ls = new ArrayList<EdocOpinion>();
        }
        return ls;
    }
    
    public List<EdocOpinion> getSenderOpinions(Long summaryId){
        String findSql = "from EdocOpinion as eo where eo.edocSummary.id = ? and eo.opinionType = ? ";
        Object[] values = {summaryId,EdocOpinion.OpinionType.senderOpinion.ordinal()};
        List<EdocOpinion> ls = super.findVarargs(findSql, values);
        if(Strings.isEmpty(ls)){
            ls = new ArrayList<EdocOpinion>();
        }
        return ls;
    }
    
    public void deleteOpinionBySummaryId(Long summaryId)
    {
        String hsql="delete from EdocOpinion as eo where eo.edocSummary.id=?";
        super.bulkUpdate(hsql, null, summaryId);
    }

    /**
     * 根据公文id查询根据节点元素绑定的排序了的意见,只读取每个节点每个人的最后处理意见，回退处理多次，只取最后一次
     * 
     * @param summaryId
     *            公文id
     * @param policy
     *            节点元素名
     * @param sortType
     *            排序方式
     * @param isOnlyShowLast
     *            同一个人的意见，是否只显示最新的一条
     * @param isbound  查询的意见是绑定的还是未绑定的意见  
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<EdocOpinion> findLastSortOpinionBySummaryIdAndPolicy(
            long summaryId, List<String> policy, String sortType,
            boolean isOnlyShowLast,boolean isBound) {
        List ls = null;
        Map parameterMap = new HashMap();
        parameterMap.put("summaryId", summaryId);
        if(policy!=null &&!policy.isEmpty()){
            parameterMap.put("policy", policy);
        }

        if (isOnlyShowLast) {
            StringBuilder hqlBuffer = new StringBuilder();
            hqlBuffer
                    .append("select edocOpinion from EdocOpinion as edocOpinion ")
                    .append(",OrgLevel as orgLevel, OrgMember as orgMember ")
                    .append(" where orgLevel.id = orgMember.orgLevelId")
                    .append(" and orgMember.id = edocOpinion.createUserId ")
                    .append(" and edocOpinion.id in ")
                    .append("   (")
                    .append("   SELECT tempEdocOption1.id ")
                    .append("   FROM EdocOpinion as tempEdocOption1 ")
                    .append("   WHERE tempEdocOption1.createTime = ")
                    .append("       (")
                    .append("       SELECT MAX(tempEdocOption2.createTime) ")
                    .append("       FROM EdocOpinion tempEdocOption2 ")
                    .append("       WHERE tempEdocOption2.createUserId = tempEdocOption1.createUserId")
                    .append("       AND tempEdocOption2.edocSummary.id = :summaryId")
                    //不显示暂存代办的意见。
                    .append("       and tempEdocOption2.opinionType <> :opinionType ");
            
                    parameterMap.put("opinionType", EdocOpinion.OpinionType.provisionalOpinoin.ordinal());
                    
                    if(policy!= null && !policy.isEmpty()){
                        if(isBound){
                            if(policy.contains("niwen") || policy.contains("dengji")){ //拟文登记policy 为null
                                hqlBuffer.append(" and ( tempEdocOption2.policy in (:policy) or   tempEdocOption2.policy is null)");
                            }else{
                                hqlBuffer.append(" and  tempEdocOption2.policy in (:policy) ");
                            }
                        }else{
                            if(policy.contains("niwen") || policy.contains("dengji")){
                                hqlBuffer.append(" and tempEdocOption2.policy not in (:policy) ");
                            }else{
                                hqlBuffer.append(" and (tempEdocOption2.policy not in (:policy) or tempEdocOption2.policy is null) ");
                            }
                        }
                    }
                    hqlBuffer.append("      )")
                    .append("   AND tempEdocOption1.edocSummary.id = :summaryId ")
                    .append("   )")
                    .append(" and edocOpinion.edocSummary.id = :summaryId");
                    
                    hqlBuffer.append(" order by ");
                    if ("0".equals(sortType)) {
                        hqlBuffer.append(" edocOpinion.createTime");
                    } else if ("1".equals(sortType)) {
                        hqlBuffer.append("edocOpinion.createTime desc");
                    } else if ("2".equals(sortType)) {
                        //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                        //如果人员编号相同，按时间顺序排（先处理的排在前）
                        hqlBuffer.append("orgLevel.levelId desc,orgMember.sortId asc,edocOpinion.createTime asc ");
                    }
                     else if ("4".equals(sortType)) {
                            hqlBuffer.append("orgLevel.levelId asc,orgMember.sortId asc,edocOpinion.createTime asc ");
                        } 
                    else if ("3".equals(sortType)) {
                        hqlBuffer.append("edocOpinion.departmentSortId, orgLevel.levelId desc,orgMember.sortId asc,edocOpinion.createTime asc");
                    //增加一种意见显示方式：按照人员序号升序显示
                     } else if ("5".equals(sortType)) {
                            hqlBuffer.append("orgMember.sortId asc,edocOpinion.createTime desc  ");
                     } else
                        hqlBuffer.append(" edocOpinion.createTime");
            ls = find(hqlBuffer.toString(), -1, -1, parameterMap);
        } else {
            // 全部显示
            /*
             * SELECT a.* FROM edoc_opinion a, v3x_org_department b,
             * v3x_org_level d, v3x_org_member c
             * 
             * WHERE b.ID = c.org_department_id AND d.ID = c.org_level_id AND
             * c.ID = a.create_user_id AND a.edoc_id = 1791605713944201898
             * 
             * ORDER BY b.sort_id ,d.sort_id,a.create_time ;
             */

            //List<Object> objects = new ArrayList<Object>();
            //objects.add(summaryId);
            //objects.add(policy);
            StringBuilder hqlBuffer = new StringBuilder();
            hqlBuffer
                    .append(
                            "select edocOpinion from EdocOpinion as edocOpinion,  OrgLevel as orgLevel, OrgMember as orgMember ")
                    .append(" where orgLevel.id = orgMember.orgLevelId")
                    .append(" and orgMember.id = edocOpinion.createUserId")
                    .append(" and edocOpinion.edocSummary.id = :summaryId");
                    if(policy!=null &&!policy.isEmpty()){
                        if(isBound){
                            if(policy.contains("niwen") || policy.contains("dengji")){ //拟文登记policy 为null
                                hqlBuffer.append(" and ( edocOpinion.policy in (:policy) or   edocOpinion.policy is null)");
                            }else{
                                hqlBuffer.append(" and  edocOpinion.policy in (:policy) ");
                            }
                        }else{
                            if(policy.contains("niwen") || policy.contains("dengji")){
                                hqlBuffer.append(" and edocOpinion.policy not in (:policy) ");
                            }else{
                                hqlBuffer.append(" and (edocOpinion.policy not in (:policy) or edocOpinion.policy is null) ");
                            }
                        }
                    }
                    hqlBuffer.append(" order by ");
                    if ("0".equals(sortType)) {
                        hqlBuffer.append(" edocOpinion.createTime");
                    } else if ("1".equals(sortType)) {
                        hqlBuffer.append("edocOpinion.createTime desc");
                    }  else if ("2".equals(sortType)) {
                        hqlBuffer.append("orgLevel.levelId desc,orgMember.sortId asc,edocOpinion.createTime asc ");
                    } 
                    /*puyc*/
                    else if ("4".equals(sortType)) {
                        hqlBuffer.append("orgLevel.levelId asc,orgMember.sortId asc,edocOpinion.createTime asc ");
                    } 
                    else if ("3".equals(sortType)) {
                        hqlBuffer.append("edocOpinion.departmentSortId, orgLevel.levelId desc,orgMember.sortId asc,edocOpinion.createTime asc");
                    }else
                        hqlBuffer.append("edocOpinion.createTime");
            ls = find(hqlBuffer.toString(), -1, -1, parameterMap);
        }
        return ls;
    }
    
    //以前性能优化---现在注释掉
    /*
    public List getOptionListId(long summaryId, List<String> policy, String sortType,boolean isOnlyShowLast,boolean isBound){
        List ls = null;
        Map parameterMap = new HashMap();
        parameterMap.put("summaryId", summaryId);
        if(policy!=null &&!policy.isEmpty()){
            parameterMap.put("policy", policy);
        }
        if (isOnlyShowLast) {
            StringBuffer subHqlBuffer = new StringBuffer();
            subHqlBuffer.append("   SELECT tempEdocOption1.id, tempEdocOption1.policy ")
            .append("   FROM EdocOpinion as tempEdocOption1 ")
            .append("   where tempEdocOption1.opinionType != ").append(EdocOpinion.OpinionType.provisionalOpinoin.ordinal());
//          .append("   and tempEdocOption1.policy in (:policy) ")
            
            if(policy!= null && !policy.isEmpty()){
                if(isBound){
                    if(policy.contains("niwen") || policy.contains("dengji")){ //拟文登记policy 为null
                        subHqlBuffer.append(" and ( tempEdocOption1.policy in (:policy) or   tempEdocOption1.policy is null)");
                    }else{
                        subHqlBuffer.append(" and  tempEdocOption1.policy in (:policy) ");
                    }
                }else{
                    if(policy.contains("niwen") || policy.contains("dengji")){
                        subHqlBuffer.append(" and tempEdocOption1.policy not in (:policy) ");
                    }else{
                        subHqlBuffer.append(" and (tempEdocOption1.policy not in (:policy) or tempEdocOption1.policy is null) ");
                    }
                }
            }
            
            subHqlBuffer.append("   and tempEdocOption1.edocSummary.id = :summaryId ");
            
            //GOV-4333 【公文管理】-【收文管理】-【分发】-【已分发】，对已分发的收文增加附言后附言在文单里没有显示
            //因为意见表中，拟文附言后，edoc_opinion表的policy是空的，和其他意见节点不一样，所以这里的处理方式区分处理
            //其他意见才分组，拟文时就不用了
            if(policy.contains("niwen") || policy.contains("dengji")){
                
                subHqlBuffer.append("   and tempEdocOption1.createTime = ")
                .append("       (")
                .append("       SELECT MAX(tempEdocOption2.createTime) ")
                .append("       FROM EdocOpinion tempEdocOption2 ")
                .append("       WHERE tempEdocOption2.edocSummary.id = :summaryId")
                .append("       and ( tempEdocOption2.policy in (:policy) or   tempEdocOption2.policy is null)")
                .append("       )");
                
            }else{
                subHqlBuffer.append("   and tempEdocOption1.createTime = ")
                .append("       (")
                .append("       SELECT MAX(tempEdocOption2.createTime) ")
                .append("       FROM EdocOpinion tempEdocOption2 ")
                .append("       WHERE tempEdocOption2.edocSummary.id = :summaryId")
                .append("       and tempEdocOption2.createUserId = tempEdocOption1.createUserId")
                .append("       and tempEdocOption2.policy = tempEdocOption1.policy " )
                .append("       )")
                .append("   group by tempEdocOption1.policy,tempEdocOption1.id "); 
            }
            
            
            ls = find(subHqlBuffer.toString(),-1,-1,parameterMap); 
        }
        return ls;
    }
    */
    
    
    
    
    
    /**
     * lijl添加,把显示最后一条里的hql抽取了 出来,获取文单的所有审批人最后的一条意见的ID
     * @param summaryId
     * @param policy
     * @param sortType
     * @param isOnlyShowLast
     * @param isBound
     * @return
     */
    public List getOptionListId(long summaryId, List<String> policy, String sortType,boolean isOnlyShowLast,boolean isBound){
        List ls = null;
        Map parameterMap = new HashMap();
        parameterMap.put("summaryId", summaryId);
        if(policy!=null &&!policy.isEmpty()){
            parameterMap.put("policy", policy);
        }
        if (isOnlyShowLast) {
        	StringBuilder subHqlBuffer = new StringBuilder();
            subHqlBuffer.append("   SELECT tempEdocOption1.id ")
            .append("   FROM EdocOpinion as tempEdocOption1 ")
            .append("   WHERE tempEdocOption1.createTime = ")
            .append("       (")
            .append("       SELECT MAX(tempEdocOption2.createTime) ")
            .append("       FROM EdocOpinion tempEdocOption2 ")
            .append("       WHERE tempEdocOption2.edocSummary.id = :summaryId")
            .append("       and tempEdocOption2.createUserId = tempEdocOption1.createUserId ")
            
            
            //不显示暂存代办的意见。
            .append("       and tempEdocOption2.opinionType <> :opinionType ");
            parameterMap.put("opinionType", EdocOpinion.OpinionType.provisionalOpinoin.ordinal());
            if(policy!= null && !policy.isEmpty()){
                if(isBound){
                    if(policy.contains("niwen") || policy.contains("dengji")){ //拟文登记policy 为null
                        subHqlBuffer.append(" and ( tempEdocOption2.policy in (:policy) or   tempEdocOption2.policy is null)");
                    }else{
                        subHqlBuffer.append(" and  tempEdocOption2.policy in (:policy) ");
                    }
                }else{
                    if(policy.contains("niwen") || policy.contains("dengji")){
                        subHqlBuffer.append(" and tempEdocOption2.policy not in (:policy) ");
                    }else{
                        subHqlBuffer.append(" and (tempEdocOption2.policy not in (:policy) or tempEdocOption2.policy is null) ");
                    }
                }
            } 
            
            subHqlBuffer.append("       )")
            .append("   AND tempEdocOption1.edocSummary.id = :summaryId ");
            
            ls = find(subHqlBuffer.toString(), -1, -1, parameterMap);
        }
        return ls;
    }
    
    /**
     * lijl添加,通过同公文ID和审批节点和状态获取意见信息
     * @param edocid 公文ID
     * @param policy 节点类型'shenpi...'
     * @param state 状态 0正常,1不显示,2表示退回的意见
     * @return List
     */
    public List getOptionListId(Long edocid,List<String> policy,int state){
        List list=null;
        Map map=new HashMap();
        map.put("edocid",edocid);
        map.put("policy",policy);
        map.put("state",state);
        String hql="select eo.id from EdocOpinion as eo where eo.edocSummary.id=:edocid and eo.policy in (:policy) and state=:state";
        return list = find(hql,map);
    }
    
    //以前性能优化---现在注释掉
//  public List getOptionListId(Long edocid,List<String> policy,int state){
//      List list=null;
//      Map map=new HashMap();
//      map.put("edocid",edocid);
//      map.put("policy",policy);
//      map.put("state",state);
//      String hql="select eo.policy,eo.id from EdocOpinion as eo where eo.edocSummary.id=:edocid and eo.policy in (:policy) and state=:state order by policy ";
//      return list = find(hql,-1,-1,map);
//  }
    
    //以前性能优化---现在注释掉
    /*
    public List<Object[]> findLastSortOpinionBySummaryIdAndPolicy(
            long summaryId, List<String> policy, String sortType,
            boolean isOnlyShowLast,boolean isBound,String optionType) {
        List ls = null;
        Map parameterMap = new HashMap();
        if (isOnlyShowLast) {
            
            List allOpinionId = new ArrayList();
            //为了不让意见有重复了，最后一次的先将ID存入Map中
            Map m = new HashMap();
            //获取文单的最后意见的所有ID
            List lsOpinion=getOptionListId(summaryId,policy,sortType,isOnlyShowLast,isBound);
            for(int i=0;i<lsOpinion.size();i++){
                Object[] lsOp = (Object[])lsOpinion.get(i);
                String onePolicy = String.valueOf(lsOp[0]);
                long opinionId = Long.parseLong(String.valueOf(lsOp[0]));
                
                allOpinionId.add(opinionId);
                m.put(opinionId, "");
            }
            int state=0;  
            List list=null;
            
            //保留最后意见
            if("1".equals(optionType)){
                //不做任何处理
                if(lsOpinion.size()<1){
                    allOpinionId=null;
                }
            }
            //审批人选择,其他保留最后意见
            else if("3".equals(optionType)){
                state=Integer.parseInt(optionType);
                
                if(lsOpinion.size()<1){
                    allOpinionId=null;
                }else{
                    //如果审批人选择的是,显示所有信息,则把state是0的查询出来,并合并lsOpinionId.addAll(list);
                    List lsOpinion2 = getOptionListId(summaryId,policy,0);
                    for(int i=0;i<lsOpinion2.size();i++){
                        Object[] lsOp = (Object[])lsOpinion2.get(i);
                        String onePolicy = String.valueOf(lsOp[0]);
                        long opinionId = Long.parseLong(String.valueOf(lsOp[1]));
                        if(m.get(opinionId) == null)
                        allOpinionId.add(opinionId);
                    }
                }
            }
            parameterMap.put("summaryId", summaryId);
            StringBuffer hqlBuffer = new StringBuffer();
            hqlBuffer.append("select edocOpinion,dept.name,v3xAccount.name from EdocOpinion as edocOpinion ")
                    .append(", V3xOrgDepartment as dept, V3xOrgLevel as orgLevel, V3xOrgMember as orgMember,V3xOrgAccount as v3xAccount")
                    .append(" where dept.id = orgMember.orgDepartmentId")
                    .append(" and orgLevel.id = orgMember.orgLevelId")
                    .append(" and orgMember.id = edocOpinion.createUserId and v3xAccount.id = dept.orgAccountId");
                    if(allOpinionId!=null&&!allOpinionId.isEmpty()){
                        hqlBuffer.append(" and edocOpinion.id in (:lsOpinionId)");//原来这里边的hql被抽取出来成了一个方法getOptionListId
                        parameterMap.put("lsOpinionId",allOpinionId);
                    }else{
                        hqlBuffer.append(" and edocOpinion.id in (0)");//原来这里边的hql被抽取出来成了一个方法getOptionListId
                    }
                    hqlBuffer.append(" and edocOpinion.edocSummary.id = :summaryId") 
                    .append(" order by ");
                    if ("0".equals(sortType)) {
                        hqlBuffer.append(" edocOpinion.createTime");
                    } else if ("1".equals(sortType)) {
                        hqlBuffer.append("edocOpinion.createTime desc");
                    } else if ("2".equals(sortType)) {//职务降序(levelid越小，职务越大)
                        //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                        //如果人员编号相同，按时间顺序排（先处理的排在前）
                        hqlBuffer.append("orgLevel.levelId asc,orgMember.code asc,edocOpinion.createTime asc ");
                    } else if ("4".equals(sortType)) {//职务升序(levelid越小，职务越大)
                        //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                        //如果人员编号相同，按时间顺序排（先处理的排在前）
                        hqlBuffer.append("orgLevel.levelId desc,orgMember.code asc,edocOpinion.createTime asc ");
                    }else if ("3".equals(sortType)) {
                        hqlBuffer.append("dept.sortId, orgLevel.levelId desc,orgMember.code asc,edocOpinion.createTime asc");
                    }else
                        hqlBuffer.append(" edocOpinion.createTime");
            ls = find(hqlBuffer.toString(), -1, -1, parameterMap);
            
        }
        else {
            // 全部显示
            
            parameterMap.put("summaryId", summaryId);
            if(policy!=null &&!policy.isEmpty()){
                parameterMap.put("policy", policy);
            }
            List<Object> objects = new ArrayList<Object>();
            objects.add(summaryId);
            objects.add(policy);
            StringBuffer hqlBuffer = new StringBuffer();
            hqlBuffer
                    .append(
                            "select edocOpinion,dept.name,v3xAccount.name from EdocOpinion as edocOpinion, V3xOrgDepartment as dept, V3xOrgLevel as orgLevel, V3xOrgMember as orgMember,V3xOrgAccount as v3xAccount")
                    .append(" where dept.id = orgMember.orgDepartmentId")
                    .append(" and orgLevel.id = orgMember.orgLevelId");
                    if("4".equals(optionType)){//state=2表示是退回时的意见
                        hqlBuffer.append(" and (edocOpinion.state = 0 or edocOpinion.state = 2)");
                    }
                    hqlBuffer.append(" and orgMember.id = edocOpinion.createUserId and v3xAccount.id = dept.orgAccountId")
                    .append(" and edocOpinion.edocSummary.id = :summaryId");
                    if(policy!=null &&!policy.isEmpty()){
                        if(isBound){
                            if(policy.contains("niwen") || policy.contains("dengji")){ //拟文登记policy 为null
                                hqlBuffer.append(" and ( edocOpinion.policy in (:policy) or   edocOpinion.policy is null)");
                            }else{
                                hqlBuffer.append(" and  edocOpinion.policy in (:policy) ");
                            }
                        }else{
                            if(policy.contains("niwen") || policy.contains("dengji")){
                                hqlBuffer.append(" and edocOpinion.policy not in (:policy) ");
                            }else{
                                hqlBuffer.append(" and (edocOpinion.policy not in (:policy) or edocOpinion.policy is null) ");
                            }
                        }
                    }
                    hqlBuffer.append(" order by ");
                    if ("0".equals(sortType)) {
                        hqlBuffer.append(" edocOpinion.createTime");
                    } else if ("1".equals(sortType)) {
                        hqlBuffer.append("edocOpinion.createTime desc");
                    } else if ("2".equals(sortType)) {//职务降序(levelid越小，职务越大)
                        //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                        //如果人员编号相同，按时间顺序排（先处理的排在前）
                        hqlBuffer.append("orgLevel.levelId asc,orgMember.code asc,edocOpinion.createTime asc ");
                    } else if ("4".equals(sortType)) {//职务升序(levelid越小，职务越大)
                        //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                        //如果人员编号相同，按时间顺序排（先处理的排在前）
                        hqlBuffer.append("orgLevel.levelId desc,orgMember.code asc,edocOpinion.createTime asc ");
                    } else if ("3".equals(sortType)) {
                        hqlBuffer.append("dept.sortId, orgLevel.levelId desc,orgMember.code asc,edocOpinion.createTime asc");
                    }else
                        hqlBuffer.append("edocOpinion.createTime");
            ls = find(hqlBuffer.toString(), -1, -1, parameterMap);
        }
        
        return ls;
    }
    
    */
    
    
    
    
    /**
     * lijl重写findLastSortOpinionBySummaryIdAndPolicy方法
     * 根据公文id查询根据节点元素绑定的排序了的意见,只读取每个节点每个人的最后处理意见，回退处理多次，只取最后一次
     * @param summaryId 公文id
     * @param policy 节点元素名
     * @param sortType 排序方式
     * @param isOnlyShowLast 同一个人的意见，是否只显示最新的一条
     * @param isbound 查询的意见是绑定的还是未绑定的意见  
     * @param option 意见显示格式
     * @return List<Object[]>
     */
    @SuppressWarnings({ "rawtypes", "unused" })
    public List<EdocOpinion> findLastSortOpinionBySummaryIdAndPolicy(long summaryId, List<String> policy, String sortType, boolean isOnlyShowLast,boolean isBound,String optionType) {
        List ls = null;
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        
        if (isOnlyShowLast) {
            //获取文单的最后意见的所有ID
            List lsOpinionId = getOptionListId(summaryId,policy,sortType,isOnlyShowLast,isBound); 
            int state=0;
            List list=null;
            //保留最后意见
            if(OpinionDisplaySetEnum.DISPLAY_LAST.getValue().equals(optionType)){
                //不做任何处理
                if(lsOpinionId.size()<1){
                    lsOpinionId=null;
                }
            }
            parameterMap.put("summaryId", summaryId);
            StringBuilder hqlBuffer = new StringBuilder();
            //性能优化--只能控制一张表orgLevel，其他表不管是否排序，确实都需要查。--start
            String showTable="";
            String showWhere="";
            if ("2".equals(sortType) || "3".equals(sortType) || "4".equals(sortType)) {
                showTable=", OrgLevel as orgLevel ";
                showWhere=" and (orgLevel.id = orgMember.orgLevelId or orgMember.orgLevelId=-1) ";
            }
            //性能优化--只能控制一张表orgLevel，其他表不管是否排序，确实都需要查。--end
            hqlBuffer.append("select edocOpinion from EdocOpinion as edocOpinion "+showTable+" , OrgMember as orgMember ");
            hqlBuffer.append(" where orgMember.id = edocOpinion.createUserId ");
            hqlBuffer.append(" and edocOpinion.state in(0,2) ");
            if ("2".equals(sortType) || "3".equals(sortType) || "4".equals(sortType)) {
                hqlBuffer.append(showWhere);
            }
    
            if(lsOpinionId!=null && !lsOpinionId.isEmpty()){
                hqlBuffer.append(" and edocOpinion.id in (:lsOpinionId)");//原来这里边的hql被抽取出来成了一个方法getOptionListId
                parameterMap.put("lsOpinionId",lsOpinionId);
            }else{
                hqlBuffer.append(" and edocOpinion.id in (0)");//原来这里边的hql被抽取出来成了一个方法getOptionListId
            }
            hqlBuffer.append(" and edocOpinion.edocSummary.id = :summaryId");
            hqlBuffer.append(" order by ");
            if ("0".equals(sortType)) {
                hqlBuffer.append(" edocOpinion.createTime");
            } else if ("1".equals(sortType)) {
                hqlBuffer.append(" edocOpinion.createTime desc ");
            } else if ("2".equals(sortType)) {//职务降序(levelid越小，职务越大)
                //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                //如果人员编号相同，按时间顺序排（先处理的排在前）
                hqlBuffer.append(" orgLevel.levelId asc,orgMember.sortId asc,edocOpinion.createTime asc ");
            } else if ("4".equals(sortType)) {//职务升序(levelid越小，职务越大)
                //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（大的在前），
                //如果人员编号相同，按时间顺序排（先处理的排在前）
                hqlBuffer.append(" orgLevel.levelId desc,orgMember.sortId desc,edocOpinion.createTime asc ");
            }else if ("3".equals(sortType)) {
                hqlBuffer.append(" edocOpinion.departmentSortId, orgLevel.levelId desc,orgMember.sortId asc,edocOpinion.createTime asc ");
           //增加一种意见显示方式：按照人员序号升序显示
            } else if ("5".equals(sortType)) {
                hqlBuffer.append("orgMember.sortId asc ,edocOpinion.createTime desc ");
            }else{
                hqlBuffer.append(" edocOpinion.createTime ");
            }
            ls = find(hqlBuffer.toString(), -1, -1, parameterMap);
        } else {
            // 全部显示
            parameterMap.put("summaryId", summaryId);
            if(policy!=null && !policy.isEmpty()){
                parameterMap.put("policy", policy);
            }
            //参数
            //List<Object> objects = new ArrayList<Object>();
            //objects.add(summaryId);
            //objects.add(policy);
            
            StringBuilder hqlBuffer = new StringBuilder();
            //性能优化--只能控制一张表orgLevel，其他表不管是否排序，确实都需要查。--start
            String showTable="";
            String showWhere="";
            if ("2".equals(sortType) || "3".equals(sortType) || "4".equals(sortType)) {
                showTable=", OrgLevel as orgLevel ";
                showWhere=" and (orgLevel.id = orgMember.orgLevelId or orgMember.orgLevelId=-1) ";
            }
            //性能优化--只能控制一张表orgLevel，其他表不管是否排序，确实都需要查。--end
            hqlBuffer.append("select edocOpinion from EdocOpinion as edocOpinion "+showTable+" , OrgMember as orgMember ");
            hqlBuffer.append(" where orgMember.id = edocOpinion.createUserId ");
            hqlBuffer.append(" and edocOpinion.state in(0,2)");
            if ("2".equals(sortType) || "3".equals(sortType) || "4".equals(sortType)) {
                hqlBuffer.append(showWhere);
            }
            
            if(OpinionDisplaySetEnum.DISPLAY_BACK_OFF_SELECT.getValue().equals(optionType)) {//指定回退人选择意见
                hqlBuffer.append(" and (edocOpinion.state = 0 or edocOpinion.state = 2)");
            }
            hqlBuffer.append(" and edocOpinion.edocSummary.id = :summaryId");
            if(policy!=null &&!policy.isEmpty()){
                if(isBound){
                    if(policy.contains("niwen") || policy.contains("dengji")){ //拟文登记policy 为null
                        hqlBuffer.append(" and ( edocOpinion.policy in (:policy) or   edocOpinion.policy is null)");
                    }else{
                        hqlBuffer.append(" and  edocOpinion.policy in (:policy) ");
                    }
                }else{
                    if(policy.contains("niwen") || policy.contains("dengji")){
                        hqlBuffer.append(" and edocOpinion.policy not in (:policy) ");
                    }else{
                        hqlBuffer.append(" and (edocOpinion.policy not in (:policy) or edocOpinion.policy is null) ");
                    }
                }
            }
            hqlBuffer.append(" order by ");
            if ("0".equals(sortType)) {
                hqlBuffer.append(" edocOpinion.createTime ");
            } else if ("1".equals(sortType)) {
                hqlBuffer.append(" edocOpinion.createTime desc ");
            } else if ("2".equals(sortType)) {//职务降序(levelid越小，职务越大)
                //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（小的在前），
                //如果人员编号相同，按时间顺序排（先处理的排在前）
                hqlBuffer.append(" orgLevel.levelId asc,orgMember.sortId asc,edocOpinion.createTime asc ");
            } else if ("4".equals(sortType)) {//职务升序(levelid越小，职务越大)
                //sunj:公文意见框按职务级别排时，如果职务级别相同，按人员编号排（大的在前），
                //如果人员编号相同，按时间顺序排（先处理的排在前）
                hqlBuffer.append(" orgLevel.levelId desc,orgMember.sortId desc,edocOpinion.createTime asc ");
            } else if ("3".equals(sortType)) {
                hqlBuffer.append(" edocOpinion.departmentSortId, orgLevel.levelId desc,orgMember.sortId asc,edocOpinion.createTime asc");
            //增加一种意见显示方式：按照人员序号升序显示
             } else if ("5".equals(sortType)) {
                    hqlBuffer.append("orgMember.sortId asc,edocOpinion.createTime desc  ");
             }else{
                hqlBuffer.append(" edocOpinion.createTime ");
            }
            ls = find(hqlBuffer.toString(), -1, -1, parameterMap);
        }
        if ("2".equals(sortType) || "3".equals(sortType) || "4".equals(sortType)) {
            //OA-48284 文单设置保留所有意见，文单拟文意见设置为按部门顺序排序，同一个人多次增加附言，查看文单，只能看到这个人的第一次的意见，其余的未保留
            //因为当为拟文或登记时opinion中的affairId为0了
            if(Strings.isNotEmpty(ls) && !(policy.contains("niwen") || policy.contains("dengji"))) {
                List list = new ArrayList();
                Map<Long, Long> idMap = new HashMap<Long, Long>();
                for (int i = 0; i < ls.size(); i++) {
                    EdocOpinion opinion = (EdocOpinion) ls.get(i);
                    if(opinion != null) {
                    	if(isOnlyShowLast) {
							if(idMap.containsKey(opinion.getAffairId())) {
								continue;
							} else {
								idMap.put(opinion.getAffairId(), opinion.getAffairId());
								list.add(ls.get(i));
							}
						} else {
							list.add(ls.get(i));
						}
                    }
                }
                return list;
            }
        }
        return ls;
    }
    
    
    /**
     * 根据公文id查询根据节点元素绑定的排序了的意见
     * 
     * @param summaryId
     *            公文id
     * @param policy
     *            节点元素名
     * @return
     */
    public List findEdocOpinionBySummaryIdAndPolicy(long summaryId,
            String policy, String sortType) {
        /*
         * SELECT a.* FROM edoc_opinion a, v3x_org_department b, v3x_org_level
         * d, v3x_org_member c
         * 
         * WHERE b.ID = c.org_department_id AND d.ID = c.org_level_id AND c.ID =
         * a.create_user_id AND a.edoc_id = 1791605713944201898
         * 
         * ORDER BY b.sort_id ,d.sort_id,a.create_time ;
         */
        List<Object> objects = new ArrayList<Object>();
        objects.add(summaryId);
        objects.add(policy);
        StringBuilder hqlBuffer = new StringBuilder();
        hqlBuffer
                .append(
                        "select edocOpinion from EdocOpinion as edocOpinion, OrgLevel as orgLevel, OrgMember as orgMember ")
                .append(" where orgLevel.id = orgMember.orgLevelId").append(
                        " and orgMember.id = edocOpinion.createUserId").append(
                        " and edocOpinion.edocSummary.id = ?").append(
                        " and edocOpinion.policy =?").append(
                        " order by ");
        if ("0".equals(sortType)) {
            hqlBuffer.append(" edocOpinion.createTime");
        } else if ("1".equals(sortType)) {
            hqlBuffer.append("edocOpinion.createTime desc");
        } else if ("2".equals(sortType)) {
            hqlBuffer.append("orgLevel.levelId");
        } else if ("3".equals(sortType)) {
            hqlBuffer.append("edocOpinion.departmentSortId");
        }else
            hqlBuffer.append("edocOpinion.createTime");
        return find(hqlBuffer.toString(), -1, -1, null, objects);
    }
    
    /**
     * lijl添加,通过edocId,userId,oplicy查询同一用户同一文单的所有意见
     * @param edocId 公文单ID
     * @param userId 用户ID
     * @param oplicy 审批类型,'shenpi,tuihui...'
     * @return List<EdocOpinion>
     */
    public List<EdocOpinion> findEdocOpinion(Long edocId,Long userId,String oplicy){
        String hql="from EdocOpinion as eo where eo.edocSummary.id=? and eo.createUserId=? and eo.policy=? order by eo.createTime desc";
        Object[]values={edocId,userId,oplicy};
        List<EdocOpinion> ls=super.findVarargs(hql, values);
        return ls;
    }
    
    
    /**
     * changyi添加,通过edocId,userId,oplicy,affairId查询同一用户同一文单同一流程中 其中一个节点已经提交过的所有意见
     * 这个主要是用于处理 后台配置文单的意见显示为  退回时办理人选择覆盖方式，其他情况保留所有意见，如果办理人选择 只显示最后的意见，那么就要将这个节点已经提交过的意见状态设置为1
     * @param edocId 公文单ID
     * @param userId 用户ID
     * @param oplicy 审批类型,'shenpi,tuihui...'
     * @param AFFAIRID 
     * @return List<EdocOpinion>
     */
    public List<EdocOpinion> findEdocOpinionByAffairId(Long edocId,Long userId,String oplicy,List<Long> affairIds){
        String hql="from EdocOpinion as eo where eo.edocSummary.id=:id and eo.createUserId=:userId and eo.policy=:policy" +
                " and eo.affairId in(:affairIds) order by eo.createTime desc";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", edocId);
        map.put("userId", userId);
        map.put("policy", oplicy);
        map.put("affairIds", affairIds);
        
//        Object[]values={edocId,userId,oplicy,affairIds};
        List<EdocOpinion> ls=super.find(hql, map);
        return ls;
    }
    
    
    /**
     * lijl重写上边方法,退回时把以前是0的状态改为2
     * @param 
     */
    public void update(Long edocId,Long userid,String oplicy,int newstate,int oldstate){
        String hql="update EdocOpinion as eo set eo.state=? where eo.edocSummary.id=? and eo.createUserId=? and eo.policy=? and eo.state=?";
//      super.getHibernateTemplate().update(hql, new Object[]{edocId,userid,oplicy,newstate,oldstate});
        super.bulkUpdate(hql,null,newstate,edocId,userid,oplicy,oldstate);
    }
    
    public List<String> findEdocOpinionByEdocSummaryId(Long edocId){
        String hql="select distinct eo.policy from EdocOpinion as eo where eo.edocSummary.id=:id ";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("id", edocId);
        List<String> ls=super.find(hql, map);
        return ls;
    }
    
    public List<EdocOpinion> findEdocOpinionByAffairId(Long affairId){
        String hql="from EdocOpinion as eo where eo.affairId=:affairId ";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("affairId", affairId);
        return super.find(hql, map);
    }
    
    public List<EdocOpinion> findReportToSupAccountOpinions(List<Long> affairIds,long subEdocId){
        String hql="from EdocOpinion where affairId in (:affairIds) and subEdocId = :subEdocId  ";
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("subEdocId", subEdocId);
        map.put("affairIds", affairIds);
        List<EdocOpinion> ls=super.find(hql, map);
        return ls;
    }
    
    /**
     * 
     * @Description : 删除下级意见
     * @Date        : 2014年11月14日下午2:21:42
     * @param subOpinionIds
     */
    public void delOptionBySubOpId(List<Long> subOpinionIds){
        
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("subOpinionIds", subOpinionIds);
        
        String hql = "delete from EdocOpinion where subOpinionId in(:subOpinionIds)";
        
        super.bulkUpdate(hql,map);
    }
    
    /**
     * 
     * @Description : 获取下级意见
     * @Author      : xuqiangwei
     * @Date        : 2014年11月14日下午2:21:17
     * @param subOpinionIds
     * @return
     */
    public List<EdocOpinion> getOptionBySubOpId(List<Long> subOpinionIds){
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("subOpinionIds", subOpinionIds);
        
        String findSql = "from EdocOpinion where subOpinionId in(:subOpinionIds)";
        List<EdocOpinion> ls=super.find(findSql, map);
        if(Strings.isEmpty(ls)){
            ls = new ArrayList<EdocOpinion>();
        }
        return ls;
    }
    
    /**
     * 删除意见-逻辑删除
     * @param idList
     */
    public void deleteOpinionById(List<Long> idList) {
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("state", 1);
        map.put("idList", idList);
        
        String hql = "update from EdocOpinion set state=:state where id in (:idList)";
        
        super.bulkUpdate(hql, map);
    }
    
}
