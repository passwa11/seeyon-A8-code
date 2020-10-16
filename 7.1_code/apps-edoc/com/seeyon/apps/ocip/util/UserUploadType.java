package com.seeyon.apps.ocip.util;

public class UserUploadType {
	//常量定义
		/**未上报 1*/
		public static final Integer JOIN_UN = 1;
		/**对外节点处理中 2*/
		public static final Integer JOIN_DEAL= 2;
		/**冲突账号 3*/
		public static final Integer JOIN_CONFLICT = 3;
		/**对外节点存储失败（同步失败）4*/
		public static final Integer JOIN_FAILD = 4;
		/**新增修改成功 5*/
		public static final Integer JOIN_SUCCESS = 5;
		/**删除成功 6*/
		public static final Integer JOIN_DELETE_SUCCESS = 6;
}
