package com.seeyon.apps.govdoc.constant;

import com.seeyon.apps.edoc.enums.EdocEnum.edocType;

public final class GovdocEnum {
	
	//操作类型枚举
	public enum OperationType {
        send, //发送
        save, //存为草稿，保存待发
        wait, //暂存待办
        finish, //正常提交
        template, //模板
        noworkflow,
        //不存流程
        personalTemplate,
        pTemplateText,
        stepBack,//回退
        stepStop,//终止
        appointStepBack //指定回退
    }
	
	//公文类型枚举，收文，发文
	public static enum EdocTypeEnum {sendEdoc,recEdoc,signReport, distributeEdoc, edocRegister};
	
	//公文文号，内部文号
    public static enum MarkTypeFieldEnum{edocMark,edocInMark,edocSignMark};
    
    //新公文枚举 发文,收文,交换,签报
    //bpm-edoc-cfg 用于工作流的NPS等参数  不能改名字
    public static enum govdocTypeEnum {govdocSend,govdocRec,govdocSign,govdocExchange};
    
	public enum OldEdocTypeEnum {
		send,//发文
		rec,//收文
		sign//签报
	}
	
	public enum DocTemplateTypeEnum {
		word,//公文套红模板--正文
		script//公文套红模板--文单
	}
	
    public enum GovdocOpenFromEnum {
    	listPending,//待办中打开公文
    	listDone,//已办中打开公文
    	listSent,//已发中打开公文
    	listWaitSend,//待发中打开公文
    	glwd//关联文档打开公文
    	//文档中心打开公文
    	//回退列表中打开公文
    	//流程追溯-撤销列表中打开公文
    	//流程追溯-回退列表中打开公文
    	//表单查询/公文查询/统计/登记簿中打开公文
    }
    
    public static enum GovdocPluginEnum {
    	edoc,
    	meeting,
    	collaboration,
    	index,
    	doc
    }
    
    public static enum GovdocRoleEnum {
    	
    }
    
    public static enum GovdocResCodeEnum {
    	F20_govdocPending,//待办列表
    	F20_govDocDone,//已办列表
    	F20_gocDovSend,//已发列表
    	F20_govDocWaitSend,//待发列表
    	F20_govDocSendManage,//发文管理
    	F20_receiveManage,//收文管理
    	F20_signReport,//签报管理
    	F20_fawenNewQuickSend,//快速发文
    	F20_newSend,//发文拟文
    	F20_newDengji,//收文登记
    	F20_newSign,//签报拟文
    	F20_search,//公文查询
    	F20_stat,//公文统计
    	F20_recSendStatistics,//收发统计
    	F20_sendandreportAuth,//发文登记薄
    	F20_recandreportAuth,//收文登记薄
    	F20_signandreportAuth,//签报登记薄
    	F20_supervise,//公文督办
    	F20_govdocExchange,//公文交换
    	F20_ocipSwicth,//组织机构上报
    	F20_ocipLog//交换操作日志
    }
    
    public static enum GovdocProcessTypeEnum {
    	govdocRecHandle(1),//办件
    	govdocRecRead(2);//阅件
    	
    	private int key;
    	GovdocProcessTypeEnum(int key) {
			this.key = key;
		}
		public int key() {
			return key;
		}
    }
    
    public static enum GovdocWorkflowTypeEnum {
    	formedoc,//表单公文
		oldedoc//老公文
    }
    
    //公文数据来源类型
    public static enum GovdocFromTypeEnum {
		inner,//内部数据
		api,//来自rest接口api调用
		ocip,//来自ocip数据
		m3sso,//单点登录多G6
		other//其它
	}
    
    //公文内部交换类型
	public static enum GovdocExchangeTypeEnum {
		jiaohuan,//默认交换
		lianhe,//联合发文
		zhuansw,//转收文
		zhuanfw//转发文
	}
	
    public static enum OldExchangeNodePolicyEnum {
    	oldfasong,
    	oldqianshou,
    	olddengji,
    	oldfenfa
    }
    
    public enum NewGovdocFrom {
    	template, //拟文-来自调用模板 
    	resend, //拟文-来自重复发起
    	waitSend, //拟文-来自待发
    	distribute, //拟文-来自分办
    	bizconfig,//拟文-来自业务生成器
    	trans//拟文-来自转发
    }
    
    public enum GovdocNewParamEnum {
    	isQuickSend, //是否快速发文 true/false
    	templateId,//调用模板
    	from,//来自界面
    	sub_app,//公文种类 1,2,3,4
    	affairId,//编辑affairId
    	summaryId,//本次编辑的公文ID，用于编辑
    	signSummaryId,//签收后跳转后分办界面，参数为签收summaryId
    	distributeType,//签收后分办类型 normal直接分办 yes签收后分办 no签收后不分办 later签收稍后分办
    	distributeAffairId,//签收流程-分办节点AffairId
    	distributeContentDataId,//签收流程-公文单数据ID
    	distributeContentTemplateId,//签收流程-公文单ID
    	forwardText,//转发标题
    	forwardAffairId,//转发AffairId
    	meetingSummaryId,//会议记要转公文，参数为会议纪要ID
    	projectId;//来自项目拟文
    }
    
    public enum GovdocSummaryParamEnum {
    	openFrom,//来自
    	affairId,//当前查看affairId
    	summaryId,//当前查看summaryId，affairId非空时，summaryId为空
    	processId,//当affairId与summaryId为空时，已知流程ID打开公文
    	operationId, //文单节点权限ID
    	formMutilOprationIds,//文单多视图节点权限ID
    	contentAnchor,//锚点，用于消息打开的时候倒叙去查询谁处理的消息(功能暂未使用)
    	isJointly,//入口：联合发文标识
    	extFrom,//入口：致信打开协同的时候会传递这个参数ucpc
    	leaderPishiType,//入口：代领导批示列表打开，列表标识(功能暂未迁移)
    	trackTypeRecord,//入口：流程追溯回退列表链接，表示追溯类型
    	dumpData,//入口：表示来自分库历史表
    	lenPotent,//入口：文档中心，配合openFrom使用，固定3位数，分别表示3种权限，值域:1或0
    	pigeonholeType//归档类型0单位归档(模板设置的归档)，1部门归档（其他方式的归档）
    }
    
    /**
     * 公文操作枚举
     * @author tanggl
     *
     */
    public enum GovdocActionEnum {
    	fromNew(0),//新建公文
    	fromEdit(1),//编辑公文
    	fromTemplate(2),//调用模板
    	fromFenban(3),//分办公文
    	fromResend(4),//重复发起
    	send(5),//发送 
    	draft(6),
    	finish(7),
    	zcdb(8),
    	stepback(9),
    	stepstop(10),
    	cancel(11),
    	takeback(12);
    	
    	private int key;
    	GovdocActionEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return key;
		}
    }

    /**
     * 分办类型
     * @author tanggl
     *
     */
	public enum DistributeTypeEnum {
		normal,//直接分办
		yes,
		no,
		later
	}
    
    public enum MarkTypeEnum {
    	doc_mark(0),
		serial_no(1),
		sign_mark(2);
    	
		private int key;
		MarkTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return key;
		}
    }
    
    public enum MarkCategoryCodeEnum {
    	small(0),
		big(1);
    	
		private int key;
		
		MarkCategoryCodeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return key;
		}
    }
    
    public enum MarkReserveTypeEnum {
    	up(1),
		down(2);
    	
		private int key;
		
		MarkReserveTypeEnum(int key) {
			this.key = key;
		}
		public int getKey() {
			return key;
		}
    }
    
    /**
     * 接收者类型
     * @author Administrator
     *
     */
    public enum GovdocExchangeOrgType{
		department(0,"部门"),
		account(1,"单位"),
		extrnalAccount(2,"外部单位");
		private int key;
		private String value;
		private GovdocExchangeOrgType(int key,String value){
			this.key = key;
			this.value = value;
		}
		
		public static GovdocExchangeOrgType getGovdocExchangeOrgType(int key){
			for(GovdocExchangeOrgType e:GovdocExchangeOrgType.values()){
				if(e.getKey() == key){
					return e;
				}
			}
			return null;
		}
		public int getKey() {
			return key;
		}
		public void setKey(int key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
    
  	/**
  	 * 交换流转状态枚举
  	 */
    public static enum TransferStatus{
		/**
		 * 默认值为-1
		 */
		defaultStatus(-1,""),
		/**
		 * 未结束(发文)
		 */
		sendNotEnd(0,"edoc.unend"),
		/**
		 * 已结束（发文）
		 */
		sendEnd(1,"edoc.ended"),
		/**
		 * 已分送（发文）
		 */
		sendPublished(2,"govdoc.stat.flowstat.published"),
		/**
		 * 待签收
		 */
		waitSigned(3,"govdoc.stat.flowstat.waitReceived"),
		/**
		 * 已签收（收文）
		 */
		receiveSigned(4,"govdoc.stat.flowstat.received"),
		/**
		 * 已分办（电子分办）
		 */
		receiveFenbanEletric(5,"govdoc.stat.flowstat.distributed"),
		/**
		 * 已分办（收文登记）
		 */
		receiveFenbanRegister(6,"govdoc.stat.flowstat.distributed"),
		/**
		 * 已签收且被分办了(系统暂时没用到，慎用)
		 */
		receiveSignedFenban(7,"govdoc.stat.flowstat.distributed"),
		/**
		 * 回退待发（通过回退，进入待发）
		 */
		stepbackWaitSend(8,""),
		/**
		 * 撤销待发（撤销流程，进入待发）
		 */
		repealWaitSend(9,""),		
		/**
		 * 撤销待发（撤销流程，进入待发） --省级专版
		 */
		takebackWaitSend(10,""),		
		/**
		 * 分送取回后，暂存待办的状态  --省级专版
		 */
		takebackZCDB(11,"");

		private int key;
		private String label;

		public int getKey() {
			return key;
		}

		public String getLabel() {
			return label;
		}

		private TransferStatus(int key, String label){
			this.key = key;
			this.label = label;
		}

		public static boolean isType(TransferStatus transferStatus,int ordinal){
			TransferStatus status = TransferStatus.values()[ordinal];
			if (status == transferStatus){
				return true;
			}else {
				return false;
			}
		}

		public static boolean isNotType(TransferStatus transferStatus,int ordinal){
			return !isType(transferStatus, ordinal);
		}

		public static TransferStatus getByKey(int key){
			for(TransferStatus transferStatus : TransferStatus.values()){
				if (transferStatus.key==key){
					return transferStatus;
				}
			}
			return null;
		}

	}
    
    /**
	 * 
	 * 数据交换状态
	 * @author Administrator
	 *
	 */
	public enum ExchangeDetailStatus{
		waitSend(0,"待交换"),
		waitSign(1,"待签收"),
		hasSign(2,"已签收"),
		hasFenBan(3,"已分办"),
		beingProcessed(4,"进行中"),
		hasSignOff(5,"签收停办"),
		hasBack(10,"已回退"),
		hasCancel(11,"已撤销"),
		hasStop(12,"已终止"),
		ended(13,"已结束"),
		voidByTakeback(14,"取回作废"), //省级专版，公文结束后可以取回。交换数据处于作废状态
		draftFenBan(15,"分办待发");//涉及到升级，但因数据很少，暂未升级
		
		private int key;
		private String value;
		private ExchangeDetailStatus(int key,String value){
			this.key = key;
			this.value = value;
		}
		
		public static ExchangeDetailStatus getExchangeDetailStatus(int key){
			int[] sign = {ExchangeDetailStatus.hasFenBan.getKey(),ExchangeDetailStatus.hasSignOff.getKey(),ExchangeDetailStatus.draftFenBan.getKey()};
			for (int i : sign) {
				if(key == i){
					return ExchangeDetailStatus.hasSign;
				}
			}
			for(ExchangeDetailStatus e:ExchangeDetailStatus.values()){
				if(e.getKey() == key){
					return e;
				}
			}
			return null;
		}

		public int getKey() {
			return key;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	
    /**
     * 画流程图的时候,根据应用类型选择节点权限
     * @param isGovdocType:1发文,2收文,3签报,4交换
     * @return
     */
    static public String getGovdocAppName(int isGovdocType)
    {
    	edocType [] values=edocType.values();
    	if(isGovdocType<0 || isGovdocType>values.length){return "";}
    	return govdocTypeEnum.values()[isGovdocType-1].name();    	
    }
}
