package com.seeyon.apps.synorg.util;

import java.util.List;

import com.seeyon.ctp.organization.bo.OrganizationMessage.OrgMessage;

/**
 * @author Yang.Yinghai
 * @date 2015-12-14下午4:27:20
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class ErrorMessageUtil {

    public static String getErrorMessageString(List<OrgMessage> mErrorList) throws Exception {
        for(OrgMessage o : mErrorList) {
            switch(o.getCode().ordinal()) {
                case 1 :
                    return "单位名称重复";
                case 2 :
                    return "单位简称重复";
                case 3 :
                    return "单位编码重复";
                case 4 :
                    return "单位管理员名称重复";
                case 5 :
                    return "单位下存在未删除的组织模型实体";
                case 6 :
                    return "单位存在有效的部门不允许停用";
                case 7 :
                    return "单位存在有效角色不允许停用";
                case 8 :
                    return "单位存在有效岗位不允许停用";
                case 9 :
                    return "单位存在有效岗位不允许停用";
                case 10 :
                    return "单位存在有效子单位不允许停用";
                case 11 :
                    return "单位存在有效组不允许停用";
                case 12 :
                    return "单位下存在人员不允许停用";
                case 13 :
                    return "部门名称重复";
                case 14 :
                    return "部门存在成员";
                case 15 :
                    return "部门存在组";
                case 16 :
                    return "父部门ID为空";
                case 17 :
                    return "父部门禁用";
                case 18 :
                    return "与父部门相同（检查父部门是否是自己）";
                case 19 :
                    return "父部门是自己的子部门";
                case 20 :
                    return "岗位名称重复";
                case 21 :
                    return "岗位存在成员";
                case 22 :
                    return "职级存在成员";
                case 23 :
                    return "存在职务级别的映射";
                case 24 :
                    return "人员所在部门不可用";
                case 25 :
                    return "人员所在岗位不可用";
                case 26 :
                    return "人员所在职务不可用";
                case 27 :
                    return "人员副岗和主岗重复";
                case 28 :
                    return "人员存在授权印章";
                case 29 :
                    return "人员不存在";
                case 30 :
                    return "人员帐号名称重复";
                case 31 :
                    return "职级存在成员";
                case 32 :
                    return "PATH重复";
                case 33 :
                    return "登录名不存在";
                case 34 :
                    return "角色不存在";
                case 35 :
                    return "基准岗位已引用";
                case 36 :
                    return "添加的人员数量大于单位剩余的可注册数量，不允许添加人员";
                case 37 :
                    return "存在职务级别的映射";
                case 38 :
                    return "单位内存在有效人员不允许停用";
                case 39 :
                    return "单位存在有效的部门不允许停用";
                case 40 :
                    return "单位存在有效角色不允许停用";
                case 41 :
                    return "单位存在有效岗位不允许停用";
                case 42 :
                    return "单位存在有效职务不允许停用";
                case 43 :
                    return "单位存在有效子单位不允许停用";
                case 44 :
                    return "单位存在有效组不允许停用";
                case 45 :
                    return "人员编码重复";
                case 46 :
                    return "部门编码重复";
                case 47 :
                    return "人员有公共信息管理员或者审核员等身份，或其他为清理事项，删除失败";
                case 48 :
                    return "单位自定义登录页地址不能重复";
                case 49 :
                    return "部门下存在启用子部门，不允许停用";
                default :
                    return "未知错误";
            }
        }
        return "未知错误";
    }
}
