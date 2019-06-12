package com.seeyon.apps.customFieldCtrl.constants;

/**
 * 周刘成   2019/5/16
 */
public enum SelectPeople {

    //field0001:人员编号
    //field0002:岗位编号
    //field0003:人员姓名
    //field0004:岗位名称

    field0001("人员编号"),
    field0002("岗位编号"),
    field0003("人员姓名"),
    field0004("岗位名称");

    /** 枚举显示名称 */
    private String text;

    SelectPeople(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
