package com.seeyon.v3x.exchange.enums;

public class EdocExchangeMode {
		/**
		 * 交换方式枚举
		 *
		 */
	    public enum EdocExchangeModeEnum{
	    	internal(0),  // 内部(致远)公文交换
	    	sursen(1);	// 书生公文交换
		    private int key;
		    EdocExchangeModeEnum(int key) {
	            this.key = key;
	        }
	        public int getKey() {
	            return this.key;
	        }
	        public int key() {
	            return this.key;
	        }
		} 
}
