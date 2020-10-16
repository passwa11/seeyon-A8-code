package com.seeyon.v3x.edoc.enums;

public enum EdocOpenFrom {
	listWaitSend, //待发
    listSent,     //已发
    listPendingAll, //所有待办（待办+在办）
    listPending,  //待办,时间线也能打开处理，传入的参数也是这个，只有OPENFROM是这个参数的时候才会打开处理页面
    listZcdb, //在办
    listDoneAll, //所有已办（已办+已办结）
    listDone,    //已办
    listFinish,  //已办结
    supervise,    //督办
    F8Reprot,     //F8穿透统计
    glwd, //关联文档
    docLib,        //  文档中心
    favorite,     //收藏的协同
    subFlow,       //子流程查看主流程，或者主流程查看子流程
    stepBackRecord, //督办列表回退的时候打开协同
    repealRecord,    //督办列表撤销的时候打开协同
    lenPotent,//借阅
    formStatistical, //表单统计
    formQuery,//表单查询
    formRelation,//表单关联穿透
    task, //任务
    exchangeRelation, //通过交换打开
    exchangeFallback, //回退列表打开
    edocStatistics,//公文统计
    leaderPishiList//代领导批示列表
}
