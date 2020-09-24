/**
 *
 */
package com.seeyon.v3x.edoc.manager;

import com.seeyon.apps.collaboration.enums.CollaborationEnum;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.dao.EdocMarkAclDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkDAO;
import com.seeyon.v3x.edoc.dao.EdocMarkHistoryDAO;
import com.seeyon.v3x.edoc.domain.EdocMark;
import com.seeyon.v3x.edoc.domain.EdocMarkAcl;
import com.seeyon.v3x.edoc.domain.EdocMarkCategory;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocMarkHistory;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocMarkHistoryExistException;
import com.seeyon.v3x.edoc.util.Constants;
import com.seeyon.v3x.edoc.util.SharedWithThreadLocal;
import org.jfree.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 类描述：
 * 创建日期：
 *
 * @author liaoj
 * @version 1.0
 * @since JDK 5.0
 */
public class EdocMarkHistoryManagerImpl implements EdocMarkHistoryManager {

    private EdocMarkHistoryDAO edocMarkHistoryDAO;
    private OrgManager orgManager;
    private EdocMarkDAO edocMarkDAO;

    private EdocMarkAclDAO edocMarkAclDAO;

    private EdocMarkManager edocMarkManager;

    private EdocMarkDefinitionManager edocMarkDefinitionManager;

    private EdocMarkCategoryManager edocMarkCategoryManager;

    private EdocMarkReserveManager edocMarkReserveManager;

    public void setEdocMarkCategoryManager(
            EdocMarkCategoryManager edocMarkCategoryManager) {
        this.edocMarkCategoryManager = edocMarkCategoryManager;
    }

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    public EdocMarkDAO getEdocMarkDAO() {
        return edocMarkDAO;
    }


    public void setEdocMarkDAO(EdocMarkDAO edocMarkDAO) {
        this.edocMarkDAO = edocMarkDAO;
    }


    public void setEdocMarkHistoryDAO(EdocMarkHistoryDAO edocMarkHistoryDAO) {
        this.edocMarkHistoryDAO = edocMarkHistoryDAO;
    }

    public void setEdocMarkAclDAO(EdocMarkAclDAO edocMarkAclDAO) {
        this.edocMarkAclDAO = edocMarkAclDAO;
    }

    public List<EdocMarkHistory> findListByMarkDefineId(Long markDefineId) {
        return (List<EdocMarkHistory>) edocMarkHistoryDAO.findVarargs("from EdocMarkHistory where edocMarkDefinition.id=?", markDefineId);
    }

    /**
     * 方法描述：保存公文文号历史
     */
    public void save(EdocMarkHistory edocMarkHistory) {
        this.edocMarkHistoryDAO.save(edocMarkHistory);
    }


    /**
     * @方法描述: 根据公文id查找文号id
     * @param summaryId 公文Id
     */

    public Long findMarkIdBySummaryId(Long summaryId) {

        List<EdocMark> edocMark = edocMarkDAO.findBy("edocId", summaryId);

        if (null != edocMark && edocMark.size() > 0) {
            return edocMark.get(0).getId();
        } else {

            return null;
        }
    }

    public List<EdocMark> findMarkBySummaryId(Long summaryId) {
        return edocMarkDAO.findBy("edocId", summaryId);
    }

    /**
     * 方法描述： 封发后将edocMark转移到edocMarkHistory
     */
    public void afterSend(EdocSummary summary) {
        Long summaryId = summary.getId();
        String docMark = summary.getDocMark();
        User user = AppContext.getCurrentUser();
        Long userId = user.getId();
        //List<EdocMark> marks=this.findMarkBySummaryId(summaryId);
        List<EdocMark> marks = null;
        if (summary.getIsunit()) {//联合发文
            marks = edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId, docMark, summary.getDocMark2());
        } else {
            marks = edocMarkDAO.findEdocMarkByEdocIdOrDocMark(summaryId, docMark);
        }
        List<EdocMark> marksAll = new ArrayList<EdocMark>();
        if (marks == null || marks.size() == 0) {
            return;
        }
        marksAll.addAll(marks);
        int codeMode = 0;//小流水0，大流水1
        List<Long> categoryids = new ArrayList<Long>();
        for (EdocMark mark : marks) {
            categoryids.add(mark.getCategoryId());
        }
        Map<Long, Integer> categoryMap = edocMarkCategoryManager.findByIds(categoryids);
        for (EdocMark mark : marks) {
            //手写的情况不需要根据CategoryId和当前号进行查找。
            if (mark.getCategoryId() == null || mark.getCategoryId() == 0) continue;
            //小流水不需要根据categoryID来查找。
            if (categoryMap.get(mark.getCategoryId()) != null) {
                codeMode = categoryMap.get(mark.getCategoryId());
            }
            if (codeMode == 0) continue;
            List<EdocMark> marksTemp = edocMarkManager.findByCategoryAndNo(mark.getCategoryId(), mark.getDocMarkNo());
            if (marksTemp != null && marksTemp.size() > 0) {
                marksAll.addAll(marksTemp);
            }
        }

        //终止公文时， 公文文号不进入历史表
        if (summary.getFinished() && summary.getState() != CollaborationEnum.flowState.terminate.ordinal()) {
            seveEdocMarkHistory(marksAll, userId, summary, codeMode);
        }
    }

    public void seveEdocMarkHistory(List<EdocMark> edocMarks, Long userId, EdocSummary summary, int codeMode) {
        List<Long> markIds = new ArrayList<Long>();
        for (EdocMark edocMark : edocMarks) {
            if (markIds.contains(edocMark.getId())) {
                continue;
            } else {
                markIds.add(edocMark.getId());
            }

            //生成公文文号历史对象
            EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
            edocMarkHistory.setIdIfNew();
            edocMarkHistory.setEdocId(summary.getId());
            if (codeMode == 0) {//小流水
                //文单上没有doc_mark，只有doc_mark2的时候，summary.getDocMark()为空，保存的时候报错。
                //或者可以直接取edocMark.getDocMark()，不取summary.getDocMark().就没有下面这个分支了，暂时先写成这样吧。
                if (summary.getDocMark() == null) {
                    edocMarkHistory.setDocMark(edocMark.getDocMark());
                } else {
                    edocMarkHistory.setDocMark(summary.getDocMark());
                }
            } else {//大流水
                edocMarkHistory.setDocMark(edocMark.getDocMark());
            }
            edocMarkHistory.setDocMarkNo(edocMark.getDocMarkNo());
            edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
            edocMarkHistory.setCompleteTime(new Date());
            edocMarkHistory.setCreateTime(edocMark.getCreateTime());
            edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
            edocMarkHistory.setLastUserId(userId);
            edocMarkHistory.setMarkNum(edocMark.getMarkNum());
            this.save(edocMarkHistory);
            edocMarkManager.deleteEdocMark(edocMark.getId());
        }
    }

    /**
     * 将公文文号保存到历史表，并删除此文号
     * @param edocMark  公文文号对象
     * @param userId  公文文号使用人id
     */
    public void seveEdocMarkHistory(EdocMark edocMark, Long userId) {

//    	if(!this.isUsed(edocMark.getEdocId(), edocMark.getDocMark())){

        //从公文文号表中删除


        edocMarkManager.deleteEdocMark(edocMark.getId());
        //生成公文文号历史对象
        EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
        edocMarkHistory.setIdIfNew();
        edocMarkHistory.setEdocId(edocMark.getEdocId());
        edocMarkHistory.setDocMark(edocMark.getDocMark());
        edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
        edocMarkHistory.setCompleteTime(new Date());
        edocMarkHistory.setCreateTime(edocMark.getCreateTime());
        edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
        edocMarkHistory.setLastUserId(userId);
        edocMarkHistory.setMarkNum(edocMark.getMarkNum());
        this.save(edocMarkHistory);
//    	}
    }


    public EdocMarkManager getEdocMarkManager() {
        return edocMarkManager;
    }


    public void setEdocMarkManager(EdocMarkManager edocMarkManager) {
        this.edocMarkManager = edocMarkManager;
    }

    /**
     * 保存公文文号历史
     * @param edocId
     * @param edocMark
     * @param markDefinitionId
     * @param markNum
     * @param createUserId
     * @param lastUserId
     */
    public void save(Long edocId, String edocMark, Long markDefinitionId, int markNum, Long createUserId, Long lastUserId, boolean checkId, boolean autoIncrement) throws EdocMarkHistoryExistException {
        this.save(edocId, -1, edocMark, markDefinitionId, markNum, createUserId, lastUserId, checkId, autoIncrement);
    }

    public void save(Long edocId, Integer currentNo, String edocMark, Long markDefinitionId, int markNum, Long createUserId, Long lastUserId, boolean checkId, boolean autoIncrement) throws EdocMarkHistoryExistException {
        if (this.isUsed(edocMark, checkId ? edocId : null))
            throw new EdocMarkHistoryExistException();
        EdocMarkDefinition markDef = SharedWithThreadLocal.getMarkDefinition(markDefinitionId);
        if (markDef == null) {
            markDef = edocMarkDefinitionManager.queryMarkDefinitionById(markDefinitionId);
        }

        EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
        edocMarkHistory.setIdIfNew();
        edocMarkHistory.setEdocId(edocId);
        edocMarkHistory.setDocMark(edocMark);
        edocMarkHistory.setDocMarkNo(currentNo);
        edocMarkHistory.setEdocMarkDefinition(markDef);
        edocMarkHistory.setCompleteTime(new Date());
        edocMarkHistory.setCreateTime(new Date());
        edocMarkHistory.setCreateUserId(createUserId);
        edocMarkHistory.setLastUserId(lastUserId);
        edocMarkHistory.setMarkNum(markNum);
        this.save(edocMarkHistory);


        if (markDef != null) {
            EdocMarkCategory edocMarkCategory = markDef.getEdocMarkCategory();

            int oldNo = edocMarkCategory.getCurrentNo();

            int addOneNo = increatementCurrentNo(markDef, currentNo, edocMarkCategory);


            String markStr = edocMarkDefinitionManager.markDef2Mode(markDef, null, addOneNo).getMark();


            boolean isUsed = edocMarkManager.isUsed(markStr, "0", String.valueOf(AppContext.currentAccountId()));

            int count = 0;

            while (isUsed && count < 1000) {

                count++;

                addOneNo = increatementCurrentNo(markDef, addOneNo, edocMarkCategory);

                markStr = edocMarkDefinitionManager.markDef2Mode(markDef, null, addOneNo).getMark();

                isUsed = edocMarkManager.isUsed(markStr, "0", String.valueOf(AppContext.currentAccountId()));

            }

            boolean isIncreatement = oldNo != addOneNo;

            if (isIncreatement) {

                //有变化的时候才更新，否则一个文单里面有2个文号的情况，后一次的值回覆盖前面的
                edocMarkCategory.setCurrentNo(addOneNo);

                edocMarkCategoryManager.updateCategory(edocMarkCategory);
            }
        }
        setEdocMarkDefinitionPublished(markDef);
    }

    public int increatementCurrentNo(EdocMarkDefinition markDef, int currentNo, EdocMarkCategory edocMarkCategory) {

        //公文文号&小流水号支持预留文号

        int returnNo = currentNo;
        int definitionMarkType = markDef.getMarkType().intValue();

        if (definitionMarkType == 0   //公文文号
                && edocMarkCategory.getCodeMode().intValue() == 0) {//小流水

            if (currentNo >= edocMarkCategory.getCurrentNo()) {//V56-1-38 预留文号：生成新的当前文号序号
                Integer _currentNo = edocMarkReserveManager.autoMakeEdocMarkCurrentNo(markDef, currentNo);
                if (!_currentNo.equals(currentNo)) {
                    returnNo = _currentNo;
                }
            }

        } else {
            if (currentNo >= edocMarkCategory.getCurrentNo()) {
                returnNo = currentNo + 1;
            }
        }
        return returnNo;
    }


    private void setEdocMarkDefinitionPublished(EdocMarkDefinition markDef) {
        //设置已经使用。
        if (markDef != null && markDef.getStatus() != null) {
            if (markDef.getStatus().shortValue() == Constants.EDOC_MARK_DEFINITION_DRAFT) {
                markDef.setStatus(Constants.EDOC_MARK_DEFINITION_PUBLISHED);
                edocMarkDefinitionManager.updateMarkDefinition(markDef);
//	    		edocMarkDefinitionManager.saveMarkDefinition(markDef);
            }
        }
    }

    /**
     * 保存从断号中选择的文号，只用于签报
     * @param edocMarkId    断号id
     * @param edocId        公文id
     * @param markNum
     */
    public void saveMarkHistorySelectOld(Long edocMarkId, String edocMark, Long edocId, Long userId, boolean checkId) throws EdocMarkHistoryExistException {

        boolean isUsed = edocMarkManager.isUsed(edocMark, checkId ? String.valueOf(edocId) : "0", String.valueOf(AppContext.currentAccountId()));
        if (isUsed) {
            throw new EdocMarkHistoryExistException();
        }
        EdocMark mark = this.edocMarkDAO.get(edocMarkId);
        this.seveEdocMarkHistory(mark, edocId, userId);
    }

    public void setEdocMarkDefinitionManager(
            EdocMarkDefinitionManager edocMarkDefinitionManager) {
        this.edocMarkDefinitionManager = edocMarkDefinitionManager;
    }

    /**
     * 判断文号在历史表中是否使用
     * @param edocMark
     * @param edocId
     * @return true已使用；false未使用
     */
    public boolean isUsed(String edocMark, Long edocId) {
        if (Strings.isBlank(edocMark))
            return false;
        boolean flag = false;
        User user = AppContext.getCurrentUser();
        List<EdocMarkAcl> list = this.edocMarkAclDAO.findMarkAclByMarkAndEdocId(edocMark, edocId);
        if (list != null && list.size() > 0) {
            for (EdocMarkAcl acl : list) {
                if ("Account".equals(acl.getAclType())) {
                    if (user.getLoginAccount().equals(acl.getDeptId())) {
                        flag = true;
                        break;
                    }
                } else if ("Department".equals(acl.getAclType())) {
                    //文号授权给其他部门A，模板绑定该文号，部门B下人员调用该模板时也可以使用该文号
                    //那么封发时是否已经使用了该文号，就要判断文号授权的部门对应的单位了
                    try {
                        long loginAccountId = user.getLoginAccount();
                        V3xOrgDepartment dep = orgManager.getDepartmentById(acl.getDeptId());
                        if (loginAccountId == dep.getOrgAccountId().longValue()) {
                            flag = true;
                            break;
                        }
                    } catch (BusinessException e) {
                        Log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return flag;
    }

    /**
     *
     * @param edocMark
     * @param markDefId
     * @param edocId
     * @return
     */
    public boolean isUsedReserved(String edocMark, Long markDefId, Long edocId) {
        if (Strings.isBlank(edocMark))
            return false;
        boolean flag = false;
        User user = AppContext.getCurrentUser();
        List<EdocMarkAcl> list = this.edocMarkAclDAO.findMarkAclByMarkAndEdocId(edocMark, markDefId, edocId);
        if (list != null && list.size() > 0) {
            for (EdocMarkAcl acl : list) {
                if ("Account".equals(acl.getAclType())) {
                    if (user.getLoginAccount().equals(acl.getDeptId())) {
                        flag = true;
                        break;
                    }
                } else if ("Department".equals(acl.getAclType())) {
                    //文号授权给其他部门A，模板绑定该文号，部门B下人员调用该模板时也可以使用该文号
                    //那么封发时是否已经使用了该文号，就要判断文号授权的部门对应的单位了
                    try {
                        long loginAccountId = user.getLoginAccount();
                        V3xOrgDepartment dep = orgManager.getDepartmentById(acl.getDeptId());
                        if (loginAccountId == dep.getOrgAccountId().longValue()) {
                            flag = true;
                            break;
                        }
                    } catch (BusinessException e) {
                        Log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return flag;
    }

    private void seveEdocMarkHistory(EdocMark edocMark, Long edocId, Long userId) {
        edocMarkManager.deleteEdocMark(edocMark.getId());
        //生成公文文号历史对象
        EdocMarkHistory edocMarkHistory = new EdocMarkHistory();
        edocMarkHistory.setIdIfNew();
        edocMarkHistory.setEdocId(edocId);
        edocMarkHistory.setDocMark(edocMark.getDocMark());
        edocMarkHistory.setDocMarkNo(edocMark.getDocMarkNo());
        edocMarkHistory.setEdocMarkDefinition(edocMark.getEdocMarkDefinition());
        edocMarkHistory.setCompleteTime(new Date());
        edocMarkHistory.setCreateTime(edocMark.getCreateTime());
        edocMarkHistory.setCreateUserId(edocMark.getCreateUserId());
        edocMarkHistory.setLastUserId(userId);
        edocMarkHistory.setMarkNum(edocMark.getMarkNum());
        this.save(edocMarkHistory);
    }

    /**
     * @方法描述: 根据公文id删除文号
     * @param summaryId 公文Id
     */

    public void deleteMarkIdBySummaryId(Long summaryId) {
        edocMarkHistoryDAO.deleteMarkIdBySummaryId(summaryId);
    }

    public void setEdocMarkReserveManager(EdocMarkReserveManager edocMarkReserveManager) {
        this.edocMarkReserveManager = edocMarkReserveManager;
    }

}
