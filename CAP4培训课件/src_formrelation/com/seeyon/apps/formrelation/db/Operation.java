package com.seeyon.apps.formrelation.db;

/**
 * @author Fangaowei
 * <pre>
 * 数据查询类型
 * </pre>
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public enum Operation{
    // 等于
    equal("="),
    // 大于
    bigger(">"),
    // 大于等于
    bigOrEqual(">="),
    // 小于
    small("<"),
    // 小于等于
    smallOrEqual("<="),
    // like
    like("like"),
    // in
    in("in"),
    // between
    between("between"),
    // 不等于
    notEq("<>");
    
    private String opera;
    
    Operation(String opera) {
        this.opera = opera;
    }

    public String getOpera() {
        return opera;
    }
}
