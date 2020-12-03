package com.seeyon.ctp.common.barCode.uitl;

/**
 * 二维码枚举
 * Created by daiyi on 2016-1-10.
 */
public class BarEnums {

    public enum EncodeLevel {
        NO("no"),//不加密
        NORMAL("normal"),//base64 简单加密
        HIGH("high");//des深度加密

        EncodeLevel(String key) {
            setKey(key);
        }

        public static EncodeLevel getEnumByKey(String key) {
            for (EncodeLevel level : values()) {
                if (level.getKey().equals(key)) {
                    return level;
                }
            }
            return NO;
        }

        private String key;


        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
