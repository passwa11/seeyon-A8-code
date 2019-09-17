package com.seeyon.v3x.edoc.util;


/**
 * 公文签收统计
 * @author 张东
 *
 */
public class GovdocSendRecStatEnum {
	
	/**
	 * 公文签收统计
	 * @author 张东
	 *
	 */
	public enum GovdocStatListTypeEnum {
		shouWenAll(0),//来文总数
		hasSign(1),//已签收
		hasFenBan(2),//已分办
		ended(3),//已办结.
		inTimeRec(4);//及时签收
		private int key;
		
		GovdocStatListTypeEnum(int key) {
			this.key = key;
		}
		public int key() {
			return this.key;
		}
	}
	

}
