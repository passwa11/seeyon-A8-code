package com.seeyon.ctp.common.appLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用日志操作类型枚举
 *
 * @author <a href="mailto:fishsoul@126.com">Mazc</a>
 * Created on 2009-8-10
 */
public enum AppLogAction {
    /**
     * 协同_新建协同
     */
    Coll_New(101),
    /**
     * 协同_转发协同
     */
    Coll_Transmit(102),
    /**
     * 协同_撤销协同
     */
    Coll_Repeal(103),
    /**
     * 协同_归档协同
     */
    Coll_Pigeonhole(104),
    /**
     * 协同_删除
     */
    Coll_Delete(105),
    /**
     * 协同预归档删除已发
     **/
    Coll_Pigeonhole_delete(107),
    /**
     * 协同模板增删改
     */
    Coll_Template_Create(151),
    Coll_Template_Edit(152),
    Coll_Template_Delete(153),
    /**
     * 协同节点权限
     */
    Coll_FlowPrem_Create(154),
    Coll_FlowPrem_Edit(155),
    /**
     * 协同修改流程
     */
    Coll_Flow_Modify(156),
    Coll_Flow_Repeal(157),
    Coll_Flow_Stop(158),
    /**
     * 协同回退
     */
    Coll_Step_Back(159),
    /**
     * 授权变更
     **/
    Coll_TempleteAuthorize(160),
    /**
     * 模板的运动
     **/
    Coll_TempleteMove(161),
    /**
     * 流程节点超期转指定人
     */
    Coll_Flow_Node_DeadLine_2_POPLE(162),
    /**
     * 流程节点超期系统自动跳过
     */
    Coll_Flow_Node_RunCase_AutoSys(163),
    /**
     * 协同修改正文的时候重新导入了文件
     */
    Coll_Content_Edit_LoadNewFile(164),
    /**
     * 协同批量替换节点
     */
    Coll_Workflow_Replace_Node(165),
    /**
     * 修改默认节点权限
     */
    Coll_Update_Default_Node(166),
    /**
     * 转办
     */
    Coll_Tranfer(167),
    /**
     * 自动撤销协同
     **/
    Coll_Flow_Auto_Repeal(170),
    /**
     * 自动终止协同
     **/
    Coll_Flow_Auto_Stop(171),

    /**
     * 知识管理_新建文档库
     **/
    Doc_New(401),
    /**
     * 知识管理_修改文档库
     **/
    Doc_Update(402),
    /**
     * 知识管理_删除文档库
     **/
    Doc_Del(403),

    /**
     * 知识管理_新建文档
     **/
    Doc_Wd_New(421),
    /**
     * 知识管理_修改文档
     **/
    Doc_Wd_Update(422),
    /**
     * 知识管理_删除文档
     **/
    Doc_Wd_Del(423),

    /**
     * 知识管理_发表文章博客
     **/
    Blog_New(451),
    Blog_Update(450),
    /**
     * 知识管理_新建文档库
     **/
    Blog_Del(452),
    /**
     * 集团管理员修改文档库
     **/
    Doc_Update_group(404),
    /**
     * 文档夹共享全部权限的授权与变更
     */
    DocFolder_ShareAuth_Update(431),
    /**
     * 文档库管理员设置与变更
     */
    DocLib_Managers_Update(432),

    /**
     * 表单_新建表单
     */
    Form_New(201),
    /**
     * 表单_修改表单
     */
    Form_Edit(202),
    /**
     * 表单_发布表单
     */
    Form_Publish(203),
    /**
     * 表单_启用表单
     */
    Form_Start(204),
    /**
     * 表单_停用表单
     */
    Form_Stop(205),
    /**
     * 表单_修改表单所属人
     */
    Form_EditAuth(206),
    /**
     * 表单流程模板的授权与变更
     */
    Form_ChangeAuth(261),
    /**
     * 表单查询模板的授权与变更
     */
    Form_ChangeQueryAuth(262),
    /**
     * 表单统计模板的授权与变更
     */
    Form_ChangeReportAuth(263),
    /**
     * 导入pak包
     */
    Form_ImportPak(271),
    /**
     * 导出pak包
     */
    Form_ExportPak(272),
    /**
     * 删除表单
     */
    Form_Delete(273),
    /**
     * 表单批量替换节点
     */
    Form_Workflow_Replace_Node(274),
    Form_Simulation_Workflow_Replace_Node(275),

    /**
     * 业务生成器_新建
     */
    Biz_New(211),
    /**
     * 业务生成器_修改
     */
    Biz_Edit(212),
    /**
     * 业务生成器_删除
     */
    Biz_Delete(213),
    /**
     * 业务生成器_权限
     */
    Biz_Auth(214),
    /**
     * 业务生成器_导入
     */
    Biz_Import(215),
    /**
     * 业务生成器_导出
     */
    Biz_Export(216),
    /**
     * cap4_业务发布
     **/
    Biz_Publish(217),
    /******cap4在线应用安装*****/
    APP_ONLINE_SETUP(241),
    /******cap4本地应用安装*****/
    APP_LOCAL_SETUP(242),

    /**
     * cap4报表日志
     */
    Cap4_QUERY_Create(220),
    Cap4_QUERY_Update(221),
    Cap4_QUERY_Delete(222),
    Cap4_STATISTICS_Create(223),
    Cap4_STATISTICS_Update(224),
    Cap4_STATISTICS_Delete(225),
    Cap4_MULTSTATS_Create(226),
    Cap4_MULTSTATS_Update(227),
    Cap4_MULTSTATS_Delete(228),
    Cap4_SCHEDULEDSTATS_Create(229),
    Cap4_SCHEDULEDSTATS_Update(230),
    Cap4_SCHEDULEDSTATS_Delete(231),
    Cap4_SUMMARY_Create(232),
    Cap4_SUMMARY_Update(233),
    Cap4_SUMMARY_Delete(234),
    /** cap4报表日志  */


    /**
     * 综合办公管理员设置与变更
     **/
    Office_ChangeAuth(2671),

    /**
     * 新建车辆分类
     */
    Office_Auto_Category_New(2601),
    /**
     * 修改车辆分类
     */
    Office_Auto_Category_Modify(2602),
    /**
     * 删除车辆分类
     */
    Office_Auto_Category_Delete(2603),
    /**
     * 登记车辆
     */
    Office_Auto_Info_New(2604),
    /**
     * 修改车辆
     */
    Office_Auto_Info_Modify(2605),
    /**
     * 删除车辆
     */
    Office_Auto_Info_Delete(2606),
    /**
     * 批量导入车辆
     */
    Office_Auto_Info_BatchImport(2607),
    /**
     * 批量修改车辆
     */
    Office_Auto_Info_BatchModify(2608),
    /**
     * 登记驾驶员
     */
    Office_Auto_Driver_New(2609),
    /**
     * 修改驾驶员
     */
    Office_Auto_Driver_Modify(2610),
    /**
     * 删除驾驶员
     */
    Office_Auto_Driver_Delete(2611),
    /**
     * 修改车辆审批流程
     */
    Office_Auto_Process_Modify(2612),
    /**
     * 修改到期提醒
     */
    Office_Auto_Remind_Modify(2613),
    /**
     * 新建车辆申请
     */
    Office_Auto_Apply_New(2614),
    /**
     * 修改车辆申请
     */
    Office_Auto_Apply_Modify(2615),
    /**
     * 撤消车辆申请
     */
    Office_Auto_Apply_Repeal(2616),
    /**
     * 删除车辆申请
     */
    Office_Auto_Apply_Delete(2617),
    /**
     * 登记维修/保养
     */
    Office_Auto_Repair_New(2618),
    /**
     * 修改维修/保养
     */
    Office_Auto_Repair_Modify(2619),
    /**
     * 删除维修/保养
     */
    Office_Auto_Repair_Delete(2620),
    /**
     * 登记违章/事故
     */
    Office_Auto_Illegal_New(2621),
    /**
     * 修改违章/事故
     */
    Office_Auto_Illegal_Modify(2622),
    /**
     * 删除违章/事故
     */
    Office_Auto_Illegal_Delete(2623),
    /**
     * 登记保险
     */
    Office_Auto_Safety_New(2624),
    /**
     * 修改保险
     */
    Office_Auto_Safety_Modify(2625),
    /**
     * 删除保险
     */
    Office_Auto_Safety_Delete(2626),
    /**
     * 登记年检
     */
    Office_Auto_Inspection_New(2627),
    /**
     * 修改年检
     */
    Office_Auto_Inspection_Modify(2628),
    /**
     * 删除年检
     */
    Office_Auto_Inspection_Delete(2629),

    /**
     * 新建用品库
     */
    Office_Stock_House_New(2641),
    /**
     * 修改用品库
     */
    Office_Stock_House_Modify(2642),
    /**
     * 删除用品库
     */
    Office_Stock_House_Delete(2643),
    /**
     * 登记用品
     */
    Office_Stock_Info_New(2644),
    /**
     * 修改用品
     */
    Office_Stock_Info_Modify(2645),
    /**
     * 删除用品
     */
    Office_Stock_Info_Delete(2646),
    /**
     * 批量导入用品
     */
    Office_Stock_Info_BatchImport(2647),
    /**
     * 用品入库
     */
    Office_Stock_Info_Storage(2648),
    /**
     * 修改用品审批流程
     */
    Office_Stock_Process_Modify(2649),
    /**
     * 新建用品申请
     */
    Office_Stock_Apply_New(2650),
    /**
     * 修改用品申请
     */
    Office_Stock_Apply_Modify(2651),
    /**
     * 撤消用品申请
     */
    Office_Stock_Apply_Repeal(2652),
    /**
     * 删除用品申请
     */
    Office_Stock_Apply_Delete(2653),

    /**
     * 新建设备库
     */
    Office_Asset_House_New(2661),
    /**
     * 修改设备库
     */
    Office_Asset_House_Modify(2662),
    /**
     * 删除设备库
     */
    Office_Asset_House_Delete(2663),
    /**
     * 登记设备
     */
    Office_Asset_Info_New(2664),
    /**
     * 修改设备
     */
    Office_Asset_Info_Modify(2665),
    /**
     * 删除设备
     */
    Office_Asset_Info_Delete(2666),
    /**
     * 批量导入设备
     */
    Office_Asset_Info_BatchImport(2667),
    /**
     * 修改设备审批流程
     */
    Office_Asset_Process_Modify(2668),
    /**
     * 新建设备申请
     */
    Office_Asset_Apply_New(2669),
    /**
     * 修改设备申请
     */
    Office_Asset_Apply_Modify(2670),
    /**
     * 撤消设备申请
     */
    Office_Asset_Apply_Repeal(2672),
    /**
     * 删除设备申请
     */
    Office_Asset_Apply_Delete(2673),

    /**
     * 新建图书资料库
     */
    Office_Book_House_New(2681),
    /**
     * 修改图书资料库
     */
    Office_Book_House_Modify(2682),
    /**
     * 删除图书资料库
     */
    Office_Book_House_Delete(2683),
    /**
     * 登记图书资料
     */
    Office_Book_Info_New(2684),
    /**
     * 修改图书资料
     */
    Office_Book_Info_Modify(2685),
    /**
     * 删除图书资料
     */
    Office_Book_Info_Delete(2686),
    /**
     * 批量导入图书资料
     */
    Office_Book_Info_BatchImport(2687),
    /**
     * 新建图书资料申请
     */
    Office_Book_Apply_New(2688),
    /**
     * 续借图书资料申请
     */
    Office_Book_Apply_Modify(2689),
    /**
     * 撤消图书资料申请
     */
    Office_Book_Apply_Repeal(2690),
    /**
     * 删除图书资料申请
     */
    Office_Book_Apply_Delete(2691),
    /**
     * 催还图书资料申请
     */
    Office_Book_Apply_Recall(2692),

    /**
     * 公告_新建公告
     */
    Bulletin_New(501),
    /**
     * 公告_修改公告
     */
    Bulletin_Modify(502),
    /**
     * 公告_发布公告
     */
    Bulletin_Publish(503),
    /**
     * 公告_取消发布公告
     */
    Bulletin_CancelPublish(504),
    /**
     * 公告_删除公告
     */
    Bulletin_Delete(505),
    /**
     * 公告_公告发布授权
     */
    Bulletin_GrantAuth(506),
    /**
     * 公告_取消审核
     */
    Bulletin_CancelAudit(507),
    /**
     * 公告_审核通过
     */
    Bulletin_AuditPass(508),
    /**
     * 公告_审核不通过
     */
    Bulletin_AduitNotPass(509),
    /**
     * 公告_审核直接发布
     */
    Bulletin_AuditPublish(510),
    /**
     * 公告_归档公告
     */
    Bulletin_Pigeonhole(511),

    /**
     * 新闻_新建新闻
     */
    News_New(521),
    /**
     * 新闻_修改新闻
     */
    News_Modify(522),
    /**
     * 新闻_发布新闻
     */
    News_Publish(523),
    /**
     * 新闻_取消发布新闻
     */
    News_CancelPublish(524),
    /**
     * 新闻_删除新闻
     */
    News_Delete(525),
    /**
     * 新闻_新闻发布授权
     */
    News_GrantAuth(526),
    /**
     * 新闻_取消审核
     */
    News_CancelAudit(527),
    /**
     * 新闻_审核通过
     */
    News_AuditPass(528),
    /**
     * 新闻_审核不通过
     */
    News_AduitNotPass(529),
    /**
     * 新闻_审核直接发布
     */
    News_AuditPublish(530),
    /**
     * 新闻_归档新闻
     */
    News_Pigeonhole(531),

    /**
     * 调查_新建调查
     */
    Inquiry_New(541),
    /**
     * 调查_修改调查
     */
    Inquiry_Modify(542),
    /**
     * 调查_发布调查
     */
    Inquiry_Publish(543),
    /**
     * 调查_取消发布调查
     */
    Inquiry_CancelPublish(544),
    /**
     * 调查_删除调查
     */
    Inquiry_Delete(545),
    /**
     * 调查_调查发布授权
     */
    Inquiry_GrantAuth(546),
    /**
     * 调查_取消审核
     */
    Inquiry_CancelAudit(547),
    /**
     * 调查_审核通过
     */
    Inquiry_AuditPass(548),
    /**
     * 调查_审核不通过
     */
    Inquiry_AduitNotPass(549),
    /**
     * 调查_审核直接发布
     */
    Inquiry_AuditPublish(550),
    /**
     * 调查_归档调查
     */
    Inquiry_Pigeonhole(551),

    /**
     * 发布讨论文章
     */
    Bbs_Publish(561),
    /**
     * 修改讨论文章
     */
    Bbs_Modify(562),
    /**
     * 删除讨论文章
     */
    Bbs_Delete(563),
    /**
     * 讨论发布授权
     */
    Bbs_GrantAuth(564),
    /**
     * 修改讨论回复
     */
    Bbs_ModifyReply(565),
    Bbs_Pigeonhole(566),
    /**
     * 前端用户：公告发布授权与变更
     */
    Bulletin_PostAuth_Update(571),
    /**
     * 前端用户：新闻发布授权与变更
     */
    News_PostAuth_Update(572),
    /**
     * 前端用户：调查发布权授权与变更
     */
    Inquiry_PostAuth_Update(573),
    /**
     * 前端用户：讨论发布权授权与变更
     */
    Bbs_PostAuth_Update(574),

    Bbs_Border_Delete(575),
    Inquiry_Type_Delete(576),
    News_Type_Delete(577),
    Bulletin_Type_Delete(578),

    /**
     * 单位公告版块管理员和审核员设置与变更
     */
    Account_BulManagers_Update(581),
    /**
     * 单位新闻版块管理员和审核员设置与变更
     */
    Account_NewsManagers_Update(582),
    /**
     * 单位调查版块管理员和审核员设置与变更
     */
    Account_InqManagers_Update(583),
    /**
     * 单位讨论版块管理员和审核员设置与变更
     */
    Account_BbsManagers_Update(584),

    /**
     * 集团公告版块管理员和审核员设置与变更
     */
    Group_BulManagers_Update(591),
    /**
     * 集团新闻版块管理员和审核员设置与变更
     */
    Group_NewsManagers_Update(592),
    /**
     * 集团调查版块管理员和审核员设置与变更
     */
    Group_InquManagers_Update(593),
    /**
     * 集团讨论版块管理员和审核员设置与变更
     */
    Group_BbsManagers_Update(594),

    /**
     * 菜单权限_新建
     */
    MenuSec_New(901),
    /**
     * 菜单权限_修改
     */
    MenuSec_Update(902),
    /**
     * 菜单权限_删除
     */
    MenuSec_Delete(903),
    /**
     * 菜单权限_启用
     */
    MenuSec_Enable(904),
    /**
     * 菜单权限_停用
     */
    MenuSec_Disable(905),

    /**
     * 电子印章_新建
     */
    Signet_New(911),
    /**
     * 电子印章_修改
     */
    Signet_Update(912),
    /**
     * 电子印章_删除
     */
    Signet_Delete(913),

    /**
     * HR管理_新建人员档案
     */
    Hr_NewStaffInfo(701),
    /**
     * HR管理_修改人员档案
     */
    Hr_UpdateStaffInfo(702),
    /**
     * HR管理_删除人员档案
     */
    Hr_DeleteStaffInfo(703),
    /**
     * HR管理_工资移交
     */
    Hr_TransferSalary(704),

    /**
     * 组织信息管理_新建单位
     */
    Organization_NewAccount(801),
    /**
     * 组织信息管理_修改单位信息
     */
    Organization_UpdateAccount(802),
    /**
     * 组织信息管理_删除单位
     */
    Organization_DeleteAccount(803),
    /**
     * 组织信息管理_新建部门
     */
    Organization_NewDept(811),
    /**
     * 组织信息管理_修改部门信息
     */
    Organization_UpdateDept(812),
    /**
     * 组织信息管理_删除部门
     */
    Organization_DeleteDept(813),
    /**
     * 组织信息管理_删除部门及子部门
     */
    Organization_DeleteDepts(814),
    /**
     * 组织信息管理_新建岗位
     */
    Organization_NewPost(815),
    /**
     * 组织信息管理_修改岗位
     */
    Organization_UpdatePost(816),
    /**
     * 组织信息管理_删除岗位
     */
    Organization_DeletePost(817),
    /**
     * 组织信息管理_批量导入岗位
     */
    Organization_BatchAddPost(818),
    /**
     * 组织信息管理_新建职务级别
     */
    Organization_NewLevel(821),
    /**
     * 组织信息管理_修改职务级别
     */
    Organization_UpdateLevel(822),
    /**
     * 组织信息管理_删除职务级别
     */
    Organization_DeleteLevel(823),

    /**
     * 政务版--组织信息管理_新建职级
     */
    Organization_NewDutyLevel(824),
    /**
     * 政务版--组织信息管理_修改职级
     */
    Organization_UpdateDutyLevel(825),
    /**
     * 政务版--组织信息管理_删除职级
     */
    Organization_DeleteDutyLevel(820),

    /**
     * 组织信息管理_新建人员
     */
    Organization_NewMember(826),
    /**
     * 组织信息管理_修改人员
     */
    Organization_UpdateMember(827),
    /**
     * 组织信息管理_删除人员
     */
    Organization_DeleteMember(828),
    /**
     * 组织信息管理_批量导入人员
     */
    Organization_BatchAddMember(829),
    /**
     * 组织信息管理_新建系统组
     */
    Organization_NewTeam(831),
    /**
     * 组织信息管理_修改系统组
     */
    Organization_UpdateTeam(832),
    /**
     * 组织信息管理_删除系统组
     */
    Organization_DeleteTeam(833),

    /**
     * 组织信息管理_新增通讯录字段
     */
    Organization_NewAddressBookField(836),
    /**
     * 组织信息管理_修改通讯录字段
     */
    Organization_UpdateAddressBookField(837),
    /**
     * 组织信息管理_删除通讯录字段
     */
    Organization_DeleteAddressBookField(838),
    /**
     * 组织信息管理_停用通讯录字段
     */
    Organization_DisEnableAddressBookField(839),

    /**
     * 组织信息管理_修改单位角色
     */
    Organization_UpdateRole(841),
    /**
     * 组织信息管理_修改工作范围控制
     */
    Organization_UpdateWorkScope(842),
    /**
     * 组织信息管理_部门调整
     */
    Organization_MoveDept(851),
    /**
     * 组织信息管理_人员调出
     */
    Organization_MoveMember(852),

    /**
     * 组织信息管理_新建外单位
     */
    Organization_NewExternalDept(853),
    /**
     * 组织信息管理_修改外单位
     */
    Organization_UpdateExternalDept(854),
    /**
     * 组织信息管理_删除外单位
     */
    Organization_DeleteExternalDept(855),
    /**
     * 组织信息管理_新增外单位人员
     */
    Organization_NewExternalMember(856),
    /**
     * 组织信息管理_修改外单位人员
     */
    Organization_UpdateExternalMember(857),
    /**
     * 组织信息管理_删除外单位人员
     */
    Organization_DeleteExternalMember(858),
    /**
     * 组织信息管理_部门角色设置与变更
     */
    Organization_ChangeDepRole(859),
    /**
     * 组织信息管理_单位角色设置与变更
     */
    Organization_ChangeOrgRole(860),


    /**
     * 组织信息管理_修改集团信息
     */
    Organization_UpdateGroupAccount(861),
    /**
     * 组织信息管理_修改集团管理员密码
     */
    Organization_UpdateGroupAdminPassword(862),
    /**
     * 组织信息管理_人员重新分配
     */
    Organization_OrgMember(863),
    /**
     * 组织信息管理_删除未分配人员
     */
    Organization_DeleteCancelMember(864),
    /**
     * 组织信息管理_批量修改人员
     */
    Organization_BanchEditMember(865),
    /**
     * 离职办理
     **/
    Organization_MemberLeave(866),


    /**
     * 组织信息管理_集团管理员新建兼职
     */
    Organization_GroupAdminAddCntPost(871),
    /**
     * 组织信息管理_集团管理员修改兼职
     */
    Organization_GroupAdminUpdateCntPost(872),
    /**
     * 组织信息管理_集团管理员删除兼职
     */
    Organization_GroupAdminDeleteCntPost(873),
    /**
     * 组织信息管理_单位管理员修改兼职
     */
    Organization_AccountAdminUpdateCntPost(874),
    /**
     * 组织信息管理_单位管理员删除兼职
     */
    Organization_AccountAdminDeleteCntPost(875),

    /**
     * 组织信息管理_新建角色
     */
    Organization_NewRole(881),
    //修改角色沿用原来的841的key因此882预留不用
    /**
     * 组织信息管理_删除角色
     */
    Organization_DeleteRole(883),
    /**
     * 组织信息管理_启用角色
     */
    Organization_EnableRole(884),
    /**
     * 组织信息管理_停用角色
     */
    Organization_DisEnableRole(885),
    /**
     * 组织信息管理_复制角色
     */
    Organization_CopyRoles(886),
    /**
     * 组织信息管理_分配资源
     */
    Organization_RoleToResource(887),
    /**
     * 组织信息管理_分配人员
     */
    Organization_RoleToMember(888),

    /**
     * 组织信息管理_修改人员汇报人
     */
    Organization_UpdateMemberReporter(889),

    /**
     * 组织信息管理_分配许可数
     */
    Organization_ChangePermission(891),
    /**
     * 组织信息管理_Server许可数设置
     */
    Organization_ChangeServerPermission(892),
    /**
     * 组织信息管理_M1许可数设置
     */
    Organization_ChangeM1Permission(893),
    /**
     * 组织信息管理_修改人员部門
     */
    Organization_UpdateMemberDept(894),
    /**
     * 组织信息管理_修改人员崗位
     */
    Organization_UpdateMemberPost(895),
    /**
     * 组织信息管理_修改人员職務
     */
    Organization_UpdateMemberLevel(896),
    /**
     * 组织信息管理_修改人员副崗
     */
    Organization_UpdateMemberSecP(897),
    /**
     * 组织信息管理_修改人员角色
     */
    Organization_UpdateMemberRole(898),
    /**
     * 组织信息管理_修改人员工作地
     */
    Organization_UpdateMemberLocal(899),

    /**
     * 系统管理维护_修改管理员密码
     */
    Systemmanager_UpdateAdminPassWord(1080),
    /**
     * 系统管理维护_修改人员密码
     */
    Systemmanager_UpdateUserPassWord(1002),
    /**
     * 系统管理维护_修改印章密码
     */
    Systemmanager_UpdateSignetPassWord(1003),
    /**
     * 系统管理维护_修改项目负责人
     */
    Departmentmanager_UpdatePorjectResponsible(1004),
    /**
     * 系统管理维护_删除锁定用户
     */
    Systemmanager_RemoveLockUser(1005),


    /**
     * 关联项目_新建
     */
    Project_New(651),
    /**
     * 关联项目_修改
     */
    Project_Update(652),
    /**
     * 关联项目_删除
     */
    Project_Delete(653),

    /**
     * 代理人_新建
     */
    Agent_New(1111),
    /**
     * 代理人_修改
     */
    Agent_Update(1112),
    /**
     * 代理人_取消
     */
    Agent_Delete(1113),
    /**
     * 代理人_离职办理
     **/
    Agent_New_LeaveMember(1114),

    /**
     * 动态口令_启用
     */
    SmsLogin_Enable(1131),
    /**
     * 动态口令_停用
     */
    SmsLogin_Disable(1132),

    /**
     * 拟文
     */
    Edoc_Send(301),
    /**
     * 登记公文
     **/
    Edoc_RegEdoc(302),
    /**
     * 撤销公文
     **/
    Edoc_Cacel(303),
    /**
     * 转发公文
     **/
    Edoc_Forward(304),
    /**
     * 归档公文
     **/
    Edoc_PingHole(305),

    /**
     * 修改公文(发文)
     */
    Edoc_update(306),
    /**
     * 删除公文(发文)
     */
    Edoc_delete(307),
    /**
     * 分发公文(发文)
     */
    Edoc_send_distribute(308),
    /**
     * 分发公文(收文)
     */
    Edoc_receive_distribute(309),
    /**
     * 删除发文
     */
    Edoc_Delete_Send(311),
    /**
     * 删除收文
     */
    Edoc_Delete_Rec(312),
    /**
     * 删除签报
     */
    Edoc_Delete_Sign(313),
    /**
     * 转发文
     */
    Edoc_forword_send(314),
    /**
     * 移交
     */
    Edoc_Transfer(318),
    /**
     * 发送公文（公文交换)
     **/
    Edoc_Send_Exchange(321),
    /**
     * 签收公文（公文交换）
     **/
    Edoc_Sing_Exchange(322),
    /**
     * 补发公文
     **/
    Edoc_RepSend_Exchange(323),
    /**
     * 删除公文交换记录（已签收）
     **/
    Edoc_Sign_Record_Del(325),
    /**
     * 删除公文交换记录（已发送）
     **/
    Edoc_Sended_Record_Del(326),
    /**
     * 签收回退(公文交换)
     **/
    Edoc_SingReturn_Exchange(327),
    /**
     * 删除登记
     */
    Edoc_RegEdoc_Delete(328),

    /**
     * 待登记-回退
     **/
    Edoc_RegEdoc_WAIT_STEPBACK(337),

    /**
     * 登记待发-回退
     **/
    Edoc_RegEdoc_DRAFT_STEPBACK(338),

    /**
     * G6 待分发-回退
     **/
    Edoc_DISTRIBUTE_STEPBACK(339),

    /**
     * G6 分发待发-回退
     **/
    Edoc_DISTRIBUTE_DRAFT_STEPBACK(340),

    /**
     * A8收文 待发-回退
     **/
    Edoc_RegEdoc_A8_DRAFT_STEPBACK(341),

    /**
     * 公文修改默认节点权限
     */
    Edoc_Update_Default_Node(351),
    /**
     * 公文指定回退（直接提交给我）
     **/
    Edoc_Appoint_Step_Back1(357),
    /**
     * 公文指定回退（流程重走）
     **/
    Edoc_Appoint_Step_Back2(358),

    /**
     * 修改密码
     */
    Update_Personal_Password(1120),
    /**
     * 修改印章密码
     */
    Update_Signet_Password(1121),

    /**
     * 公文模板的授权与变更
     */
    Edoc_TempleteAuthorize(361),

    /**
     * 公文节点权限变更与自定义
     */
    Edoc_FlowPermModify(362),

    /**
     * 公文文号授权与变更
     */
    Edoc_MarkAuthorize(363),

    /**
     * 套红模板授权与变更
     */
    Edoc_DocTempleteAuthorize(364),

    /**
     * 公文发起权授权与变更
     */
    Edoc_SendSetAuthorize(365),

    /**
     * 公文开关设置变更
     */
    Edoc_OpenSetAuthorize(366),
    /**
     * 公文节点权限新建
     */
    Edoc_FlowPrem_Create(367),
    /**
     * 新建公文文号
     */
    Edoc_Mark_Create(368),
    /**
     * 套红模板授权与变更
     */
    Edoc_DocTempleteCreate(370),
    /**
     * 公文模板的创建
     */
    Edoc_Templete_Create(371),
    /**
     * 公文模板的删除
     */
    Edoc_Templete_Delete(372),
    /**
     * 公文模板的修改
     */
    Edoc_Templete_Update(373),
    /**
     * 公文模板的停用
     */
    Edoc_Templete_Stop(374),
    /**
     * 公文模板的启用
     */
    Edoc_Templete_Start(375),
    /**
     * 公文节点权限的删除
     */
    Edoc_FlowPerm_Delete(376),
    /**
     * 公文文号的删除
     */
    Edoc_Mark_Delete(377),
    /**
     * 公文单的创建
     */
    Edoc_Form_Crete(378),
    /**
     * 公文单的删除
     */
    Edoc_Form_Delete(379),
    /**
     * 公文单的修改
     */
    Edoc_Form_Update(380),
    /**
     * 公文单授权信息的修改
     */
    Edoc_Form_Authorize(381),
    /**
     * 公文单被设置为默认
     */
    Edoc_Form_SetDefault(382),
    /**
     * 公文套红模板的删除
     */
    Edoc_DocTemplete_Delete(383),
    /**
     * 公文元素的修改
     */
    Edoc_Element_Update(384),
    /**
     * 公文元素的停用
     */
    Edoc_Element_Stop(385),
    /**
     * 公文元素的启用
     */
    Edoc_Element_Start(386),
    /**
     * 外部单位的创建
     */
    Edoc_OutAccount_Create(387),
    /**
     * 外部单位的删除
     */
    Edoc_OutAccount_Delete(388),
    /**
     * 外部单位的修改
     */
    Edoc_OutAccount_Update(389),
    /**
     * 机构组的创建
     */
    Edoc_OrgTeam_Create(390),
    /**
     * 机构组的删除
     */
    Edoc_OrgTeam_Delete(391),
    /**
     * 机构组的修改
     */
    Edoc_OrgTeam_Update(392),
    /**
     * 公文开关恢复至默认配置
     */
    Edoc_Open_SetDefault(393),

    /**
     * 修改公文附件
     */
    Edoc_File_update(396),
    /**
     * 修改公文正文
     */
    Edoc_Content_update(395),
    /**
     * 修改公文文单
     */
    Edoc_Form_update(394),


    /**
     * 公文归档后修改
     */
    Edoc_pigeonhole_update(328),
    /**
     * 归档后修改公文发文附件
     */
    Edoc_pigeonhole_send_File_update(329),
    /**
     * 归档后修改公文发文正文
     */
    Edoc_pigeonhole_send_Content_update(330),
    /**
     * 归档后修改公文发文文单
     */
    Edoc_pigeonhole_send_Form_update(331),
    /**
     * 归档后修改公文签报附件
     */
    Edoc_pigeonhole_sign_File_update(332),
    /**
     * 归档后修改公文签报正文
     */
    Edoc_pigeonhole_sign_Content_update(333),
    /**
     * 归档后修改公文签报文单
     */
    Edoc_pigeonhole_sign_Form_update(334),
    /**
     * 删除公文交换记录（待签收）
     **/
    Edoc_PreSign_Record_Del(335),
    /**
     * 删除公文交换记录（待发送）
     **/
    Edoc_PreSend_Record_Del(336),
    /**
     * 指定退回
     */
    Edoc_StopBack(397),
    /**
     * 协同修改正文的时候重新导入了文件
     */
    Edoc_Content_Edit_LoadNewFile(398),
    /**
     * 公文批量替换节点
     */
    Edoc_Workflow_Replace_Node(399),

    /**
     * 公文文号使用：新建
     **/
    Edoc_Doc_Mark_Create(352),
    /**
     * 公文文号使用：修改
     **/
    Edoc_Doc_Mark_Update(353),
    /**
     * 内部文号使用：新建
     **/
    Edoc_Serial_No_Create(354),
    /**
     * 内部文号使用：修改
     **/
    Edoc_Serial_No_Update(355),
    /**
     * 清稿日志
     **/
    Edoc_ClearTrail(356),

    /**
     * 删除登录日志
     */
    Clear_Log_Logon(1051),
    /**
     * 删除应用日志
     */
    Clear_Log_App(1052),
    /**
     * 导出登录日志
     */
    Export_Log_Logon(1053),
    /**
     * 导出应用日志
     */
    Export_Log_App(1054),

    /**
     * 计划 新建
     */
    Plan_New(601),
    /**
     * 计划 修改
     */
    Plan_Update(602),
    /**
     * 计划 删除
     */
    Plan_Delete(603),

    /**
     * 修改会议纪要
     */
    Meeting_summary_modify(616),
    /**
     * 日程 新建
     */
    Calendar_New(621),
    /**
     * 日程 修改
     */
    Calendar_Update(622),
    /**
     * 日程 删除
     */
    Calendar_Delete(623),
    /**
     * 日程 委托
     */
    Calendar_Commission(624),

    /**
     * 工作任务
     */
    Task_Create(631),
    Task_Update(632),
    Task_Delete(633),

    /**
     * 项目负责人修改
     */
    ProjectManagerModifyed(1011),
    /**
     * 移动短信设置 集团
     */
    SMSAuthorityModify_Set(1012),
    /**
     * 移动短信重新授权 集团
     */
    SMSAuthorityModify_ReSet(1013),
    /**
     * 电子印章授权-新建
     */
    SignetAuth_New(1014),
    /**
     * 电子印章授权-修改
     */
    SignetAuthModify(1015),
    /**
     * 博客启停变更
     */
    BlogStateModify_Stop(1016),
    BlogStateModify_Start(1017),
    /**
     * 工作管理设置增删改
     */
    WorkManageAuth_New(1018),
    WorkManageAuth_Update(1019),
    WorkManageAuth_Delete(1020),
    /**
     * 移动短信权限设置变更-单位管理员
     */
    SMSAuthAccount_Set(1021),
    /**
     * 集团空间管理员设置
     */
    GroupSpaceManager_Set(1022),
    /**
     * 系统开关变更
     */
    SystemOpenModify(1023),
    /**
     * 系统开关恢复默认
     */
    SystemOpenToDefault(1024),
    /**
     * 目录服务配置变更
     */
    DirectoryConfig(1025),
    /**
     * 关联系统增删改
     */
    InterSystem_Create(1026),
    InterSystem_Update(1027),
    InterSystem_Delete(1028),
    /**
     * 扩展栏目增删改
     */
    ExtenColumnChange_Create(1029),
    ExtenColumnChange_Update(1030),
    ExtenColumnChange_Delete(1031),
    /**
     * RSS频道增删改
     */
    RssChanelChange_Create(1032),
    RssChanelChange_Update(1033),
    RssChanelChange_Delete(1034),


    // 工作时间
    /**
     * 修改集团工作时间
     */
    WorkTimeSet_Update_Group_WorkTime(1035),
    /**
     * 修改单位工作时间
     */
    WorkTimeSet_Update_Unit_WorkTime(1036),
    /**
     * 继承集团工作日
     */
    WorkTimeSet_Inherit_Group_WorkDay(1037),
    /**
     * 继承集团工作时间
     */
    WorkTimeSet_Inherit_Group_WorkTime(1038),
    /**
     * 修改工作日
     */
    WorkTimeSet_Update_WorkDay(1039),

    /**
     * 访问控制增删改
     */
    Ipcontrol_Create(1040),
    Ipcontrol_Update(1041),
    Ipcontrol_delete(1042),

    NC_Bing_Create(9901),
    NC_Bing_Delete(9902),
    NC_Group_Update(9903),
    NC_Group_Delete(9904),

    LDAP_Account_Bing_Create(4001),
    LDAP_Account_Create(4002),
    LDAP_OU_Create(4004),
    LDAP_OU_Update(4005),
    LDAP_PassWord_Update(4006),
    LDAP_Member_PassWord_Update(4007),

    /************信息报送部分****************/
    /**
     * (信息上报)新建信息
     */
    Information_Up_New(2410),
    /**
     * (信息上报)信息修改
     */
    Information_Up_Modify(2411),
    /**
     * (信息上报)信息删除
     */
    Information_Up_Delete(2412),
    /**
     * (信息上报)信息撤销
     */
    Information_Up_Cancel(2413),


    /************新添加部分****************/
    /**
     * (信息上报)发送信息
     */
    Information_Up_Send(2414),


    /**
     * (信息审核)接收
     */
    Information_Audit_Receive(2420),
    /**
     * (信息审核)审核
     */
    Information_Audit_Audit(2421),
    /**
     * (信息审核)采编
     */
    Information_Audit_Edit(2422),
    /**
     * (信息审核)签发
     */
    Information_Audit_Sign(2423),
    /**
     * (信息审核)分类
     */
    Information_Audit_Category(2424),

    /************新添加部分****************/
    /**
     * (信息审核)进行加签、减签、修改附件操作
     */
    Information_Audit_Label(2425),
    /**
     * (信息审核)审核信息时修改正文
     */
    Information_Audit_Content(2426),
    /**
     * (信息审核)审核信息时归档正文
     */
    Information_Audit_File(2427),
    /**
     * (信息审核)审核信息时加签
     */
    Information_Audit_Add(2428),
    /**
     * (信息审核)审核信息时减签
     */
    Information_Audit_remove(2429),


    /**
     * (期刊管理)新建期刊栏目
     */
    Information_Magezine_New(2430),
    /**
     * (期刊管理)修改期刊栏目
     */
    Information_Magezine_Modify(2431),
    /**
     * (期刊管理)删除期刊栏目
     */
    Information_Magezine_Delete(2432),
    /**
     * (期刊管理)新增期刊内容
     */
    Information_InfoSummary_New(2433),
    /**
     * (期刊管理)修改期刊内容
     */
    Information_InfoSummary_Modify(2434),
    /**
     * (期刊管理)删除期刊内容
     */
    Information_InfoSummary_Delete(2435),
    /**
     * (期刊管理)修改待发期刊
     */
    Information_Magezine_ModifyDraft(2473),

    /************新添加部分****************/
    /**
     * (期刊管理)撤销期刊内容
     */
    Information_Magezine_Cancel(2436),
    /**
     * (期刊管理)发布
     */
    Information_Magezine_Publish(2437),
    /**
     * (期刊管理)取消发布
     */
    Information_Magezine_Unpublish(2438),
    /**
     * (期刊管理)审核  通过可发布
     */
    Information_Magezine_PublishPass(2439),
    /**
     * (期刊管理)审核  通过不发布
     */
    Information_Magezine_PassNoPublish(2443),
    /**
     * (期刊管理)审核  退回采编
     */
    Information_Magezine_StepBack(2444),
    /**
     * (期刊管理)信息积分表中的评分
     */
    Information_Magezine_ManualRating(2445),
    /**
     * (期刊管理)修改取消发布期刊
     */
    Information_Magezine_ModifyUnpublish(2474),
    /**
     * (期刊管理)修改审核不通过期刊
     */
    Information_Magezine_ModifyNopass(2475),
    /**
     * (期刊管理)信息统计中的评分
     */
    Information_Magezine_Statistics(2488),
    /**
     * (期刊管理)信息统计中的删除
     */
    Information_Magezine_StatisticsDelete(2489),
    /**
     * (期刊管理)审核  通过可发布  未打开本地文件
     */
    Information_Magezine_NoPublishPass(2490),
    /**
     * (期刊管理)审核  通过不发布  未打开本地文件
     */
    Information_Magezine_NoPassNoPublish(2491),
    /**
     * (期刊管理)审核  退回采编  未打开本地文件
     */
    Information_Magezine_NoStepBack(2492),


    /**
     * (信息考核)评分
     */
    Information_InfoCheck_Grade(2440),
    /**
     * (信息考核)修改评分
     */
    Information_InfoCheck_ModefyGrade(2441),
    /**
     * 菜单权限授权与变更
     */
    Info_SendSetAuthorize(2442),

    /************新添加部分****************/
    /**
     * (应用设置) 报送单新建
     */
    Information_Form_CreateForm(2446),
    /**
     * (应用设置) 报送单修改
     */
    Information_Form_ModifyForm(2447),
    /**
     * (应用设置) 设置默认报送单
     */
    Information_Form_DefaultForm(2448),
    /**
     * (应用设置) 设置授权
     */
    Information_Form_Authorize(2449),
    /**
     * (应用设置) 报送单删除
     */
    Information_Form_Detele(2450),
    /**
     * (应用设置) 报送单启用
     */
    Information_Form_Enable(2451),
    /**
     * (应用设置) 报送单停用
     */
    Information_Form_Disabled(2452),
    /**
     * (应用设置) 类型管理新增
     */
    Information_Type_New(2453),
    /**
     * (应用设置) 类型管理修改
     */
    Information_Type_Modify(2454),
    /**
     * (应用设置) 类型管理删除
     */
    Information_Type_Detele(2455),

    /**
     * (应用设置) 评分标准新建
     */
    Information_Score_New(2456),
    /**
     * (应用设置) 评分标准修改
     */
    Information_Score_Modify(2457),
    /**
     * (应用设置) 评分标准启用
     */
    Information_Score_Enable(2458),
    /**
     * (应用设置) 评分标准停用
     */
    Information_Score_Disabled(2459),
    /**
     * (应用设置) 评分发布
     */
    Info_Score_Publish_Delete(2471),
    /**
     * (应用设置) 评分发布保存
     */
    Info_Score_Publish_Save(2472),


    /**
     * (应用设置) 期刊版式新建
     */
    Information_TaoHong_New(2460),
    /**
     * (应用设置) 期刊版式修改
     */
    Information_TaoHong_Modify(2461),
    /**
     * (应用设置) 期刊版式删除
     */
    Information_TaoHong_Detele(2462),
    /**
     * (应用设置) 期刊版式下载模板标签列表
     */
    Information_TaoHong_Template(2463),


    /**
     * (应用设置) 报送单元素修改
     */
    Information_Element_Modify(2464),
    /**
     * (应用设置) 报送单元素启用
     */
    Information_Element_Enable(2465),
    /**
     * (应用设置) 报送单元素停用
     */
    Information_Element_Disabled(2466),
    /**
     * (应用设置) 报送单元素导出Excel
     */
    Information_Element_Excel(2467),
    /**
     * (应用设置) 报送单元素开关设置 累计每次发布得分
     */
    Information_SwitchSet_Configiten(2468),
    /**
     * (应用设置) 报送单元素开关设置 最高得分
     */
    Information_SwitchSet_Max(2469),
    /**
     * 信息统计
     */
    Information_Stat(2470),


    /**
     * (应用设置) 应用模板新建
     */
    Information_template_New(2476),
    /**
     * (应用设置) 应用模板修改
     */
    Information_template_Modify(2477),
    /**
     * (应用设置) 应用模板删除
     */
    Information_template_Delete(2478),
    /**
     * (应用设置) 应用模板授权
     */
    Information_template_Authorize(2479),
    /**
     * (应用设置) 应用模板启用
     */
    Information_template_Enable(2480),
    /**
     * (应用设置) 应用模板停用
     */
    Information_template_Disabled(2481),


    /**
     * (应用设置) 节点权限新建
     */
    Information_permission_Create(2482),
    /**
     * (应用设置) 节点权限修改
     */
    Information_permission_Modify(2483),
    /**
     * (应用设置) 节点权限删除
     */
    Information_permission_Delete(2484),
    /**
     * (应用设置) 报送单元素设置
     */
    Information_permission_Set(2485),
    /**
     * (期刊管理)修改期刊打开本地文件
     */
    Information_Magezine_ModifyOpen(2486),
    /**
     * (期刊管理)新建期刊打开本地文件
     */
    Information_Magezine_NewOpen(2487),

    /************信息报送部分结束****************/

    /**
     * 新建会议申请
     */
    Meeting_app_new(2210),
    /**
     * 修改会议申请
     */
    Meeting_app_update(2211),
    /**
     * 撤消会议申请
     */
    Meeting_app_repeal(2212),
    /**
     * 删除会议申请
     */
    Meeting_app_delete(2213),
    /**
     * 审核会议申请
     */
    Meeting_app_audit(2214),
    /**
     * 新建会议通知
     */
    Meeting_notice_new(2220),
    /**
     * 修改会议通知
     */
    Meeting_notice_update(2221),
    /**
     * 撤消会议通知
     */
    Meeting_notice_repeal(2222),
    /**
     * 删除会议通知
     */
    Meeting_notice_delete(2223),
    /**
     * 新建会议纪要
     */
    Meeting_summary_new(2230),
    /**
     * 撤消会议纪要
     */
    Meeting_summary_repeal(2231),
    /**
     * 删除会议纪要
     */
    Meeting_summary_delete(2232),
    /**
     * 审核会议纪要
     */
    Meeting_summary_audit(2233),

    /**
     * 会议室申请
     */
    MeetingRoom_app_new(2240),
    /**
     * 撤消会议室申请
     */
    MeetingRoom_app_repeal(2241),
    /**
     * 删除会议室申请
     */
    MeetingRoom_app_delete(2242),
    /**
     * 审核会议室申请
     */
    MeetingRoom_app_audit(2243),

    /**
     * 新增会议室管理员
     */
    MeetingRoom_admin_new(2244),
    /**
     * 删除会议室管理员
     */
    MeetingRoom_admin_delete(2245),
    /**
     * 修改会议室管理员
     */
    MeetingRoom_admin_update(2246),
    /**
     * 修改会议审核权
     */
    MeetingRoom_audit_update(2247),
    /**
     * 修改领导查阅权
     */
    MeetingLeader_lookup_update(2248),

    /**
     * 会议 新建
     */
    Meeting_New(2260),
    /**
     * 会议 修改
     */
    Meeting_Update(2261),
    /**
     * 会议 删除
     */
    Meeting_Delete(2262),
    /**
     * 会议 归档
     */
    Meeting_Document(2263),

    DataDump(1061), //数据转储

    /**
     * 新建报表授权
     */
    Report_auth_new(6001),
    /**
     * 修改报表授权
     */
    Report_auth_update(6002),
    /**
     * 删除报表授权
     */
    Report_auth_delete(6003),
    Coll_cobe_new(6051),
    Coll_cobe_update(6052),
    Coll_cobe_delete(6053),
    Coll_cobe_index_update(6054),
    /**
     * 6600~6699 报表中心占用 
     */
    /**
     * 新建关联系统
     */
    LinkSystem_Add(7001),
    /**
     * 修改关联系统
     */
    LinkSystem_Modify(7002),
    /**
     * 删除关联系统
     */
    LinkSystem_delte(7003),

    /**
     * 流程知识中心-导出Excel
     */
    Process_export_excel(1401),
    /**
     * 流程知识中心授权-新建
     */
    Process_acl_save(1402),
    /**
     * 流程知识中心授权-修改
     */
    Process_acl_update(1403),
    /**
     * 流程知识中心授权-删除
     */
    Process_acl_delete(1404),

    //==========================================报表应用日志枚举=====开始=================
    /**
     * 报表数据集创建  （日志已经迁移到报表中心）v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_dataSet_create(7004),
    /**
     * 报表数据集修改
     */
    seeyonReport_dataSet_update(7005),
    /**
     * 报表数据集删除
     */
    seeyonReport_dataSet_delete(7006),

    /**
     * 报表模板分类创建
     */
    seeyonReport_category_create(7007),
    /**
     * 报表模板分类修改
     */
    seeyonReport_category_update(7008),
    /**
     * 报表模板分类删除
     */
    seeyonReport_category_delete(7009),

    /**
     * 报表模板创建 （日志已经迁移到报表中心）v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_template_create(7010),
    /**
     * 报表模板修改 （日志已经迁移到报表中心）v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_template_edit(7011),
    /**
     * 报表模板删除 （日志已经迁移到报表中心）v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_template_delete(7012),

    /**
     * 报表管理员创建 （日志已经迁移到报表中心）v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_admin_create(7013),
    /**
     * 报表管理员修改（日志已经迁移到报表中心 ） v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_admin_edit(7014),
    /**
     * 报表管理员删除（日志已经迁移到报表中心）v7.0SP1 by shuqi
     */
    @Deprecated
    seeyonReport_admin_delete(7015),


    //==========================================报表应用日志枚举=====结束=================

    //==========================================秀吧应用日志枚举=====开始=================
    /**
     * 秀吧新建
     */
    Show_Bar_new(8316),
    /**
     * 秀吧修改
     */
    Show_Bar_edit(8319),
    /**
     * 秀吧删除
     */
    Show_Bar_delete(8320),
    /**
     * 秀圈新建
     */
    Show_Bar_Circle_new(8321),
    /**
     * 秀圈删除
     */
    Show_Bar_Circle_delete(8322),
    //==========================================秀吧应用日志枚举=====结束=================

    //TODO 各自应用的操作类型往下添加
    ;
    //  标识 用于数据库存储
    private int key;

    AppLogAction(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }

    public int key() {
        return this.key;
    }

    /**
     * 根据key得到枚举类型
     *
     * @param key
     * @return
     */
    public static AppLogAction valueOf(int key) {
        AppLogAction[] enums = AppLogAction.values();
        if (enums != null) {
            for (AppLogAction enum1 : enums) {
                if (enum1.key() == key) {
                    return enum1;
                }
            }
        }
        return null;
    }

    /**
     * 得到某个模块下所有的操作类型
     */
    public static List<Integer> getModuleActionIds(int key) {
        List<Integer> result = new ArrayList<Integer>();
        AppLogAction[] enums = AppLogAction.values();
        if (enums != null) {
            for (AppLogAction enum1 : enums) {
                int k = enum1.key();
                if (key < k && k < key + 100) {
                    result.add(k);
                }
            }
        }
        return result;
    }
}