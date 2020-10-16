package com.seeyon.apps.testBPM.constant;

/**
 * 请假表单字段
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public enum QingjiaFormFieldEnum{
    shenqingren(1, "申请人"),
    bumen(2, "申请人部门"),
    kaishishijian(3, "开始时间"),
    jieshushijian(4, "结束时间"),
    chufadi(5, "始发地"),
    mudidi(6, "目的地"),
    qingjiatianshu(7, "请假天数"),
    qingjiashiyou(8, "请假事由"),
    shenpiyijian(9, "审批意见"),
    shenpileixing(10, "审批类型"),
    qingjialeixing(11, "请假类型");

    /** 枚举值 */
    private int key;

    /** 枚举显示名称 */
    private String text;

    /**
     * @param key
     * @param text
     */
    QingjiaFormFieldEnum(int key, String text) {
        this.key = key;
        this.text = text;
    }

    /**
     * @return
     */
    public int getKey() {
        return key;
    }

    /**
     * @return
     */
    public String getText() {
        return text;
    }
}
