package com.seeyon.apps.collaboration.quartz;

public enum NodeOverTimeAutoRunCheckCode {
	normal, // 正常
	specialback, // 指定回退
	mainInSpecialback,
	flowexecute, // 流程决定分支走向的人
    vouchnode,//核定节点
    isPreNewFlowFinish,//前面节点触发的子流程是否已经结束
    hasNewFlowOrHasBranchSelectPeople,//是否有子流程或者分支选人。
    hasNewFlow,//邦定了子流程
    hasBranchSelectPeople,//有分支选人
    isFormMustWrite,//是否表单必填
	exchangenode, // 交换类型
	opinionMustWrite,//意见必填
	circlelink,//环形分支
	strongValidate,//表单强制校验
	checkDee,//校验dee任务
	allOtherFalse  //其他所有不能节点超期跳过的情况。
}
