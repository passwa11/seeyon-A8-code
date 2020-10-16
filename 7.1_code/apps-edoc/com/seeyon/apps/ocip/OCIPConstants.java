package com.seeyon.apps.ocip;

public interface OCIPConstants {
	
	public static final String PLUGIN_ID = "ocip";
	/**
	 * 是否启动OCIP插件 false--不启用 true--启用
	 */
	public static final String PLUGIN_STARTUP = "ocip.plugin.isStartup";
	
	public static final String HAS_MUTI_PLUGIN = "ocip.plugin.hasMutiPlugin" ;

	public static final String USE_OCIP_DATA_ACCOUNT = "ocip.useOcipData.account";

	public static final String USE_OCIP_DATA_DEPARTMENT = "ocip.useOcipData.department";

	public static final String USE_OCIP_DATA_POST = "ocip.useOcipData.post";

	public static final String USE_OCIP_DATA_LEVEL = "ocip.useOcipData.level";

	public static final String USE_OCIP_DATA_USER = "ocip.useOcipData.user";
	
	public static final String LOCAL_RESOURCE = "ocip.sysCode";

	/**
	 * OrgProperties type 单位
	 */
	public static final Integer OrgProperties_TYPE_UNIT = 0;

	/**
	 * OrgProperties type 部门
	 */
	public static final Integer OrgProperties_TYPE_DEPARTMENT = 1;

	/**
	 * OrgProperties type 岗位
	 */
	public static final Integer OrgProperties_TYPE_POST = 2;

	/**
	 * OrgProperties type 职级
	 */
	public static final Integer OrgProperties_TYPE_LEVEL = 3;

	/**
	 * OrgProperties type 人员
	 */
	public static final Integer OrgProperties_TYPE_MEMBER = 4;

	public static final String SUCCESS = "1";
	public static final String DELETE = "1";
	public static final String ENABLE = "1";
	public static final String FAIL = "0";
	/**
	 * 陈超 用户验证选单位结果是不是来自平台
	 */
	public static final String OCIP_CHOOSE_REG_EXP  = "^\\w+\\|\\w+\\|-?[a-z0-9]+\\|[\\w\\W]+$";
	
	public static final String OCIP_ENTITY_FLAG  = "ocip_entity";
}
