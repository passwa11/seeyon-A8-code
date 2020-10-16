package com.seeyon.apps.customFieldCtrl.constants;

public enum FormFieldEnum {
	
	wuliaobianhao("物料编号"),
	wuliaomingchen("物料名称"),
	lingyongshuliang("领用数量"),
	saomaquyu("扫码区域"),
	zerenbumen("责任部门"),
	danjubianhao("单据编号"),
	lingyongren("领用人"),
	suoshubumen("所属部门"),
	lingyongshijian("领用时间"),
	lingyongwuliao("领用物料编号"),
	lingyongmc("领用物料名称"),
	price("价格");

	/** 枚举显示名称 */
    private String text;

    /**
     * @param text
     */
    private FormFieldEnum(String text) {
        this.text = text;
    }

    /**
     * @return
     */
    public String getText() {
        return text;
    }
    
}
