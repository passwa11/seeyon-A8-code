package com.seeyon.apps.synorg.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.synorg.constants.SynOrgConstants;
import com.seeyon.apps.synorg.dao.HrSecondPostDao;
import com.seeyon.apps.synorg.po.hr.HrSecondPost;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.organization.OrgConstants;
import com.seeyon.ctp.organization.OrgConstants.MemberPostType;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgRole;
import com.seeyon.ctp.organization.manager.DepartmentManager;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.v3x.common.web.login.CurrentUser;

/**
 * @author Yang.Yinghai
 * @date 2015-8-18下午5:15:47
 * @Copyright(c Beijing Seeyon Software Co.,LTD
 */
public class SyncDeptRoleManagerImpl implements SyncDeptRoleManager {

    private HrSecondPostDao hrSecondPostDao;

    /** 组织机构管理器 */
    private OrgManager orgManager;

    /** 部门管理器 */
    private DepartmentManager departmentManager;

    /** 一级目录角色列表 */
    private List<V3xOrgRole> rootRolelist = new ArrayList<V3xOrgRole>();

    /** 其它级别目录角色列表 */
    private List<V3xOrgRole> otherRolelist = new ArrayList<V3xOrgRole>();

    /** 部门主管映射表 */
    private Map<Long, String> deptAdminMap;

    /** 单位兼职人员列表 */
    private Map<String, Map<String, String>> secondMap;

    /**
     * {@inheritDoc}
     */
    public void synAllDeptRole() throws Exception {
        setCurrentUser();
        deptAdminMap = new HashMap<Long, String>();
        getSecondMap();
        System.out.println("角色同步开始！！！！");
        if(rootRolelist.size() == 0) {
            rootRolelist.add(orgManager.getRoleByName(OrgConstants.Role_NAME.DepManager.name(), SynOrgConstants.DEFAULT_ACCOUNT_ID));
            otherRolelist.add(orgManager.getRoleByName(OrgConstants.Role_NAME.DepManager.name(), SynOrgConstants.DEFAULT_ACCOUNT_ID));
            otherRolelist.add(orgManager.getRoleByName(OrgConstants.Role_NAME.DepLeader.name(), SynOrgConstants.DEFAULT_ACCOUNT_ID));
        }
        List<V3xOrgDepartment> rootdept = orgManager.getChildDeptsByAccountId(SynOrgConstants.DEFAULT_ACCOUNT_ID, true);
        System.out.println("共查询出一级部门" + rootdept.size() + "个");
        for(V3xOrgDepartment dept : rootdept) {
            // 只处理内部部门
            if(dept.getIsInternal()) {
                Map<String, String> roleMap = new HashMap<String, String>();
                String deptAdminUsers = getDeptAdminUsers(dept);
                // 将计算出来的部门主管放到MAP中，以便设置子部门的部门分管领导
                if(!deptAdminMap.containsKey(dept.getId())) {
                    deptAdminMap.put(dept.getId(), deptAdminUsers);
                }
                roleMap.put("deptrole0", deptAdminUsers);
                departmentManager.dealDeptRole(roleMap, dept, rootRolelist);
                setChildDeptRole(dept);
            }
        }
        System.out.println("角色同步结束！！！！");
    }

    /**
     * 设置人员Map
     */
    private void getSecondMap() {
        secondMap = new HashMap<String, Map<String,String>>();
        List<HrSecondPost> list = hrSecondPostDao.findAll();
        System.out.println("数据库查询出来的副岗条数：" + list.size());
        if(list != null) {
            for(HrSecondPost entity : list) {
                if(!secondMap.containsKey(entity.getDeptCode())) {
                    Map<String, String> memberMap = new HashMap<String, String>();
                    memberMap.put(entity.getUserCode(), entity.getLevelCode());
                    secondMap.put(entity.getDeptCode(), memberMap);
                } else {
                    Map<String, String> memberMap = secondMap.get(entity.getDeptCode());
                    if(!memberMap.containsKey(entity.getUserCode())) {
                        memberMap.put(entity.getUserCode(), entity.getLevelCode());
                    }
                }
            }
        }
        System.out.println("Map size():" + secondMap.size());
    }

    /**
     * 设置当前用户
     */
    private void setCurrentUser() {
        V3xOrgMember admin = orgManager.getAdministrator(SynOrgConstants.DEFAULT_ACCOUNT_ID);
        User currentUser = new User();
        currentUser.setId(admin.getId());
        currentUser.setName(admin.getName());
        currentUser.setAccountId(admin.getOrgAccountId());
        currentUser.setLoginAccount(admin.getOrgAccountId());
        currentUser.setDepartmentId(admin.getOrgDepartmentId());
        CurrentUser.set(currentUser);
        AppContext.putThreadContext(GlobalNames.SESSION_CONTEXT_USERINFO_KEY, currentUser);
    }

    /**
     * 递归设置所有子部门的部门角色
     * @param deptObj 父级部门
     * @throws Exception 异常
     */
    private void setChildDeptRole(V3xOrgDepartment deptObj) throws Exception {
        List<V3xOrgDepartment> childDepts = orgManager.getChildDepartments(deptObj.getId(), true);
        if(childDepts != null && childDepts.size() > 0) {
            for(V3xOrgDepartment dept : childDepts) {
                // 只处理内部部门
                if(dept.getIsInternal()) {
                    Map<String, String> roleMap = new HashMap<String, String>();
                    String deptAdminUsers = getDeptAdminUsers(dept);
                    // 将计算出来的部门主管放到MAP中，以便设置子部门的部门分管领导
                    if(!deptAdminMap.containsKey(dept.getId())) {
                        deptAdminMap.put(dept.getId(), deptAdminUsers);
                    }
                    roleMap.put("deptrole0", deptAdminUsers);
                    // 层级大于二以上部门分管领导为父级部门的部门主管
                    roleMap.put("deptrole1", deptAdminMap.get(deptObj.getId()));
                    departmentManager.dealDeptRole(roleMap, dept, otherRolelist);
                    setChildDeptRole(dept);
                }
            }
        }
    }

    /**
     * 根据职务级别区间找到部门的部门主管
     * @param deptId 部门ID
     * @return
     * @throws Exception
     */
    private String getDeptAdminUsers(V3xOrgDepartment dept) throws Exception {
        /**
         * 分四个区间 规则如下 1区间 01 02 用户 2区间 021 022 03 04 用户 3区间 05 06 4区间 07用户
         */
        List<V3xOrgMember> oneArea = new ArrayList<V3xOrgMember>();
        List<V3xOrgMember> twoArea = new ArrayList<V3xOrgMember>();
        List<V3xOrgMember> threeArea = new ArrayList<V3xOrgMember>();
        List<V3xOrgMember> fourArea = new ArrayList<V3xOrgMember>();
        List<V3xOrgMember> fiveArea = new ArrayList<V3xOrgMember>();
        // 先找副岗
        List<V3xOrgMember> secondList = orgManager.getMembersByDepartment(dept.getId(), true, MemberPostType.Second);
        if(secondList != null && secondList.size() != 0) {
            for(V3xOrgMember member : secondList) {
                if(member.getIsInternal() && member.getOrgLevelId() != -1) {
                    // 获取副岗的级别
                    String code = getSecondPostLevelCode(dept, member);
                    if(code.equals("01") || code.equals("02")) {
                        oneArea.add(member);
                    } else if(code.equals("021") || code.equals("022")) {
                        twoArea.add(member);
                    } else if(code.equals("03") || code.equals("04") || code.equals("041")) {
                        threeArea.add(member);
                    } else if(code.equals("05") || code.equals("06")) {
                        fourArea.add(member);
                    } else if(code.equals("07")) {
                        fiveArea.add(member);
                    }
                }
            }
        }
        // 再找主岗
        List<V3xOrgMember> mainList = orgManager.getMembersByDepartment(dept.getId(), true, MemberPostType.Main);
        if(mainList != null && mainList.size() != 0) {
            for(V3xOrgMember member : mainList) {
                if(member.getIsInternal() && member.getOrgLevelId() != -1) {
                    String code = orgManager.getLevelById(member.getOrgLevelId()).getCode();
                    if(code.equals("01") || code.equals("02")) {
                        oneArea.add(member);
                    } else if(code.equals("021") || code.equals("022")) {
                        twoArea.add(member);
                    } else if(code.equals("03") || code.equals("04") || code.equals("041")) {
                        threeArea.add(member);
                    } else if(code.equals("05") || code.equals("06")) {
                        fourArea.add(member);
                    } else if(code.equals("07")) {
                        fiveArea.add(member);
                    }
                }
            }
        }
        if(oneArea.size() > 0) {
            return getMemberString(oneArea);
        } else if(twoArea.size() > 0) {
            return getMemberString(twoArea);
        } else if(threeArea.size() > 0) {
            return getMemberString(threeArea);
        } else if(fourArea.size() > 0) {
            return getMemberString(fourArea);
        } else if(fiveArea.size() > 0) {
            return getMemberString(fiveArea);
        }
        return "";
    }

    /**
     * 查找兼职人员的级别，如果视图中没有数据则以主岗级别为准
     * @param dept 部门
     * @param member 人员
     * @return
     * @throws Exception
     */
    private String getSecondPostLevelCode(V3xOrgDepartment dept, V3xOrgMember member) throws Exception {
        Map<String, String> memberMap = secondMap.get(dept.getCode());
        if(memberMap != null && memberMap.get(member.getCode()) != null) {
            return memberMap.get(member.getCode());
        } else {
            return orgManager.getLevelById(member.getOrgLevelId()).getCode();
        }
    }

    private String getMemberString(List<V3xOrgMember> userList) {
        StringBuffer sb = new StringBuffer("");
        for(V3xOrgMember member : userList) {
            if("".equals(sb.toString())) {
                sb.append("Member|").append(member.getId()).append("|").append(member.getName()).append("|true");
            } else {
                sb.append(",Member|").append(member.getId()).append("|").append(member.getName()).append("|true");
            }
        }
        return sb.toString();
    }

    /**
     * 设置orgManager
     * @param orgManager orgManager
     */
    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    /**
     * 设置departmentManager
     * @param departmentManager departmentManager
     */
    public void setDepartmentManager(DepartmentManager departmentManager) {
        this.departmentManager = departmentManager;
    }

    /**
     * 设置hrSecondPostDao
     * @param hrSecondPostDao hrSecondPostDao
     */
    public void setHrSecondPostDao(HrSecondPostDao hrSecondPostDao) {
        this.hrSecondPostDao = hrSecondPostDao;
    }
}
