package com.seeyon.apps.govdoc.constant;

import java.util.ArrayList;
import java.util.List;

public enum GovdocAppLogAction {
	
	/**协同_删除*/
	COLL_DELETE(105),
	
	/**流程节点超期转指定人*/
    COLL_FLOW_NODE_DEADLINE_2_POPLE(162),
    /**流程节点超期系统自动跳过*/
    COLL_FLOW_NODE_RUNCASE_AUTOSYS(163),
	/**协同修改正文的时候重新导入了文件*/
    COLL_CONTENT_EDIT_LOADNEWFILE(164),

	/** 拟文*/
    EDOC_SEND(301),
    /**登记公文**/
    EDOC_REGEDOC(302),
    /**撤销公文**/
    EDOC_CACEL(303),
    /**转发公文**/
    EDOC_FORWARD(304),
    /**归档公文**/
    EDOC_PINGHOLE(305),

    /**修改公文(发文)*/
    EDOC_UPDATE(306),
    /**删除公文(发文)*/
    EDOC_DELETE (307),
    /**分发公文(发文)*/
    EDOC_SEND_DISTRIBUTE(308),
    /**分发公文(收文)*/
    EDOC_RECEIVE_DISTRIBUTE(309),
    /**
     * 删除发文
     */
    EDOC_DELETE_SEND(311),
    /**
     * 删除收文
     */
    EDOC_DELETE_REC(312),
    /**
     * 删除签报
     */
    EDOC_DELETE_SIGN(313),
    /**
     * 转发文
     */
    EDOC_FORWORD_SEND(314),
    /**
     * 回退公文
     */
    EDOC_STEP_BACK(317),
    /**
     * 移交
     */
    EDOC_TRANSFER(318),
    /**发送公文（公文交换)**/
    EDOC_SEND_EXCHANGE(321),
    /**签收公文（公文交换）**/
    EDOC_SING_EXCHANGE(322),
    /**补发公文**/
    EDOC_REPSEND_EXCHANGE(323),
    /**删除公文交换记录（已签收）**/
    EDOC_SIGN_RECORD_DEL(325),
    /**删除公文交换记录（已发送）**/
    EDOC_SENDED_RECORD_DEL(326),
    /**签收回退(公文交换)**/
    EDOC_SINGRETURN_EXCHANGE(327),
    /**
     * 删除登记
     */
    EDOC_REGEDOC_DELETE(328),
    
    /** 待登记-回退  **/
    EDOC_REGEDOC_WAIT_STEPBACK(337),
    
    /** 登记待发-回退  **/
    EDOC_REGEDOC_DRAFT_STEPBACK(338),
    
    /**G6 待分发-回退 **/
    EDOC_DISTRIBUTE_STEPBACK(339),
    
    /**G6 分发待发-回退 **/
    EDOC_DISTRIBUTE_DRAFT_STEPBACK(340),
    
    /**A8收文 待发-回退  **/
    EDOC_REGEDOC_A8_DRAFT_STEPBACK(341),
    
    /**G6 流程节点超期系统自动跳过*/
    EDOC_FLOW_NODE_RUNCASE_AUTOSYS(349),
    
    /**公文修改默认节点权限*/
    EDOC_UPDATE_DEFAULT_NODE(351),
    
    /**公文模板的授权与变更*/
    EDOC_TEMPLETEAUTHORIZE(361),

    /**公文节点权限变更与自定义*/
    EDOC_FLOWPERMMODIFY(362),

    /**公文文号授权与变更*/
    EDOC_MARKAUTHORIZE(363),

    /**套红模板授权与变更*/
    EDOC_DOCTEMPLETEAUTHORIZE(364),

    /**公文发起权授权与变更*/
    EDOC_SENDSETAUTHORIZE(365),

    /**公文开关设置变更*/
    EDOC_OPENSETAUTHORIZE(366),
    /**公文节点权限新建*/
    EDOC_FLOWPREM_CREATE(367),
    /**新建公文文号*/
    EDOC_MARK_CREATE(368),
    /**套红模板授权与变更*/
    EDOC_DOCTEMPLETECREATE(370),
    /** 公文模板的创建 */
    EDOC_TEMPLETE_CREATE(371),
     /** 公文模板的删除 */
    EDOC_TEMPLETE_DELETE(372),
     /** 公文模板的修改 */
    EDOC_TEMPLETE_UPDATE(373),
     /** 公文模板的停用 */
    EDOC_TEMPLETE_STOP(374),
     /** 公文模板的启用 */
    EDOC_TEMPLETE_START(375),
     /** 公文节点权限的删除 */
    EDOC_FLOWPERM_DELETE(376),
     /** 公文文号的删除 */
    EDOC_MARK_DELETE(377),
     /** 公文单的创建 */
    EDOC_FORM_CRETE(378),
     /** 公文单的删除 */
    EDOC_FORM_DELETE(379),
     /** 公文单的修改 */
    EDOC_FORM_UPDATE(380),
     /** 公文单授权信息的修改 */
    EDOC_FORM_AUTHORIZE(381),
     /** 公文单被设置为默认 */
    EDOC_FORM_SETDEFAULT(382),
     /** 公文套红模板的删除 */
    EDOC_DOCTEMPLETE_DELETE(383),
     /** 公文元素的修改 */
    EDOC_ELEMENT_UPDATE(384),
     /** 公文元素的停用 */
    EDOC_ELEMENT_STOP(385),
     /** 公文元素的启用 */
    EDOC_ELEMENT_START(386),
     /** 外部单位的创建 */
    EDOC_OUTACCOUNT_CREATE(387),
     /** 外部单位的删除 */
    EDOC_OUTACCOUNT_DELETE(388),
     /** 外部单位的修改 */
    EDOC_OUTACCOUNT_UPDATE(389),
     /** 机构组的创建 */
    EDOC_ORGTEAM_CREATE(390),
     /** 机构组的删除 */
    EDOC_ORGTEAM_DELETE(391),
     /** 机构组的修改 */
    EDOC_ORGTEAM_UPDATE(392),
     /** 公文开关恢复至默认配置 */
    EDOC_OPEN_SETDEFAULT(393),

    /**修改公文附件*/
    EDOC_FILE_UPDATE(396),
    /**修改公文正文*/
    EDOC_CONTENT_UPDATE(395),
    /**修改公文文单*/
    Edoc_Form_update(394),
    /**新增  公文单发布**/
    EDOC_FORM_PUBLISH(11002),
    /**文单_启用文单*/
    EDOC_FORM_START(11003),
    /**文单_停用文单*/
    EDOC_FORM_STOP(11004),
	
    /** (应用设置) 应用模板新建 */
	INFORMATION_TEMPLATE_NEW(2476),
	/** (应用设置) 应用模板修改*/
	INFORMATION_TEMPLATE_MODIFY(2477),
	/** (应用设置) 应用模板删除*/
	INFORMATION_TEMPLATE_DELETE(2478),
	/** (应用设置) 应用模板授权*/
	INFORMATION_TEMPLATE_AUTHORIZE(2479),
	/** (应用设置) 应用模板启用*/
	INFORMATION_TEMPLATE_ENABLE(2480),
	/** (应用设置) 应用模板停用*/
	INFORMATION_TEMPLATE_DISABLED(2481),
	/**授权变更**/
    COLL_TEMPLETEAUTHORIZE(160),
    /**模板的运动**/
    COLL_TEMPLETEMOVE(161),
	
	/** (应用设置) 节点权限新建 */
	INFORMATION_PERMISSION_CREATE(2482),
	/** (应用设置) 节点权限修改 */
	INFORMATION_PERMISSION_MODIFY(2483),
	/** (应用设置) 节点权限删除 */
	INFORMATION_PERMISSION_DELETE(2484),
	/** (应用设置) 报送单元素设置*/
	INFORMATION_PERMISSION_SET(2485),
	/**(期刊管理)修改期刊打开本地文件*/
	INFORMATION_MAGEZINE_MODIFYOPEN(2486),
	/**(期刊管理)新建期刊打开本地文件*/
	INFORMATION_MAGEZINE_NEWOPEN(2487),


    /**公文归档后修改*/
    EDOC_PIGEONHOLE_UPDATE(328),
    /**归档后修改公文发文附件*/
    EDOC_PIGEONHOLE_SEND_FILE_UPDATE(329),
    /**归档后修改公文发文正文*/
    EDOC_PIGEONHOLE_SEND_CONTENT_UPDATE(330),
    /**归档后修改公文发文文单*/
    EDOC_PIGEONHOLE_SEND_FORM_UPDATE(331),
    /**归档后修改公文签报附件*/
    EDOC_PIGEONHOLE_SIGN_FILE_UPDATE(332),
    /**归档后修改公文签报正文*/
    EDOC_PIGEONHOLE_SIGN_CONTENT_UPDATE(333),
    /**归档后修改公文签报文单*/
    EDOC_PIGEONHOLE_SIGN_FORM_UPDATE(334),
    /**删除公文交换记录（待签收）**/
    EDOC_PRESIGN_RECORD_DEL(335),
    /**删除公文交换记录（待发送）**/
    EDOC_PRESEND_RECORD_DEL(336),
    /**
     * 指定退回
     */
    EDOC_STOPBACK(397),
    /**协同修改正文的时候重新导入了文件*/
    EDOC_CONTENT_EDIT_LOADNEWFILE(398),
    /**公文批量替换节点*/
    EDOC_WORKFLOW_REPLACE_NODE(399),
    
    /**公文文号使用：新建**/
    EDOC_DOC_MARK_CREATE(352),
    /**公文文号使用：修改**/
    EDOC_DOC_MARK_UPDATE(353),
    /**内部文号使用：新建**/
    EDOC_SERIAL_NO_CREATE(354),
    /**内部文号使用：修改**/
    EDOC_SERIAL_NO_UPDATE(355),
    /**清稿日志**/
    EDOC_CLEARTRAIL(356),
	/**公文修改意见*/
    EDOC_MODIFY_OPINION(39901),
    /**公文删除意见*/
    EDOC_DELETE_OPINION(39902),
    /**公文回复意见*/
    EDOC_REPLY_OPINION(39903),
    /** 公文终止**/
    EDOC_STOP(39904),
    /** 公文重复自动跳过**/
    REPEAT_SKIP(344);
	
	//标识 用于数据库存储
    private int key;

    GovdocAppLogAction(int key) {
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
     * @param key
     * @return
     */
    public static GovdocAppLogAction valueOf(int key) {
    	GovdocAppLogAction[] enums = GovdocAppLogAction.values();
        if (enums != null) {
            for (GovdocAppLogAction enum1 : enums) {
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
        GovdocAppLogAction[] enums = GovdocAppLogAction.values();
        if (enums != null) {
            for (GovdocAppLogAction enum1 : enums) {
                int k = enum1.key();
                if (key<k && k<key+100) {
                    result.add(k);
                }
            }
        }
        return result;
    }

}
