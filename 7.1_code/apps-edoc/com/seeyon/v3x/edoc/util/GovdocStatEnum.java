package com.seeyon.v3x.edoc.util;

import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;

/**
 * 新公文统计枚举
 * @author 唐桂林
 *
 */
public class GovdocStatEnum {
	
	public enum StatEdocTypeEnum {
		all(-1),
		edoc_send(0),
		edoc_rec(1),
		edoc_sign(2);
		
		private int key;
		StatEdocTypeEnum(int key) {
			this.key = key;
		}
		public int key() {
			return this.key;
		}
	}
	
	/**
	 * 公文统计
	 * @author 唐桂林
	 *
	 */
	public enum GovdocStatTypeEnum {
		All, Account, Department
	}
	
	/**
	 * 公文统计流程结束过滤枚举
	 * @author 唐桂林
	 *
	 */
	public enum StatGovdocTypeEnum {
		all(-1),
		edoc_fawen(1),
		edoc_shouwen(2);
		
		private int key;
		
		StatGovdocTypeEnum(int key) {
			this.key = key;
		}
		public int key() {
			return this.key;
		}
	}
	
	/**
	 * 公文统计流程结束过滤枚举
	 * @author 唐桂林
	 *
	 */
	public enum GovdocFinishStateEnum {
		all(-1),
		pending(0),
		finished(1);
		
		private int key;
		
		GovdocFinishStateEnum(int key) {
			this.key = key;
		}
		public int key() {
			return this.key;
		}
	}
	
	/**
	 * 公文统计超期过滤枚举
	 * @author 唐桂林
	 *
	 */
	public enum GovdocOverTimeEnum {
		all(-1),
		no(0),
		yes(1);
		
		private int key;
		
		GovdocOverTimeEnum(int key) {
			this.key = key;
		}
		public int key() {
			return this.key;
		}
	}
	
	/**
	 * 新公文统计列表
	 * @author 唐桂林
	 *
	 */
	public enum GovdocStatListTypeEnum {
		fawenAll(1, ApplicationSubCategoryEnum.edoc_fawen.key(), GovdocFinishStateEnum.all.key(), false, GovdocOverTimeEnum.all.key()),//统计发文数
		fawenPending(2, ApplicationSubCategoryEnum.edoc_fawen.key(), GovdocFinishStateEnum.pending.key(), false, GovdocOverTimeEnum.all.key()),//统计发文办理中
		fawenFinished(3, ApplicationSubCategoryEnum.edoc_fawen.key(), GovdocFinishStateEnum.finished.key(), true, GovdocOverTimeEnum.all.key()),//统计发文已办结
		fawenCoverTime(4, ApplicationSubCategoryEnum.edoc_fawen.key(), GovdocFinishStateEnum.all.key(), false, GovdocOverTimeEnum.yes.key()),//统计发文超期
		shouwenAll(5, ApplicationSubCategoryEnum.edoc_shouwen.key(), GovdocFinishStateEnum.all.key(), false, GovdocOverTimeEnum.all.key()),//统计收文数
		shouwenPending(6, ApplicationSubCategoryEnum.edoc_shouwen.key(), GovdocFinishStateEnum.pending.key(), false, GovdocOverTimeEnum.all.key()),//统计收文办理中
		shouwenFinished(7, ApplicationSubCategoryEnum.edoc_shouwen.key(), GovdocFinishStateEnum.finished.key(), true, GovdocOverTimeEnum.all.key()),//统计收文已办结
		shouwenCoverTime(8, ApplicationSubCategoryEnum.edoc_shouwen.key(), GovdocFinishStateEnum.all.key(), false, GovdocOverTimeEnum.yes.key()),//统计收文超期
		govdocAll(9, StatGovdocTypeEnum.all.key(), GovdocFinishStateEnum.all.key(), false, GovdocOverTimeEnum.all.key()),//统计公文总数
		govdocPending(10, StatGovdocTypeEnum.all.key(), GovdocFinishStateEnum.pending.key(), false, GovdocOverTimeEnum.no.key()),//统计公文办理中
		govdocDone(11, StatGovdocTypeEnum.all.key(), GovdocFinishStateEnum.finished.key(), true, GovdocOverTimeEnum.all.key()),//统计公文已办结 
		govdocCoverTime(12, StatGovdocTypeEnum.all.key(), GovdocFinishStateEnum.all.key(), false, GovdocOverTimeEnum.yes.key()),//统计公文超期
		
		qianshouSend(20, -1, -1, false, -1),//签收统计发文数
		qianshouTwoDay(21, -1, -1, false, -1),//2个工作日签收数
		qianshouThreeDay(22, -1, -1, false, -1),//3-5个工作日签收
		qianshouFiveDay(23, -1, -1, false, -1),//5个工作日以上签收
		qianshouNo(24, -1, -1, false, -1);//还未签收
		
		private int key;
		private int value;
		private int state;
		private int overTime;
		private boolean finished = false;
		
		GovdocStatListTypeEnum(int key, int value, int state, boolean finished, int overTime) {
			this.key = key;
			this.value = value;
			this.state = state;
			this.finished = finished;
			this.overTime = overTime;
		}
		public int key() {
			return this.key;
		}
		public int value() {
			return this.value;
		}
		public int state() {
			return this.state;
		}
		public boolean finished() {
			return this.finished;
		}
		public int overTime() {
			return this.overTime;
		}
		public static GovdocStatListTypeEnum getEnumByKey(int key) {
            for(GovdocStatListTypeEnum bean : GovdocStatListTypeEnum.values()) {
                if(bean.key() == key) {
                    return bean;
                }
            }
            throw new IllegalArgumentException("未定义的枚举类型!key=" + key);
        }
	}
	

}
