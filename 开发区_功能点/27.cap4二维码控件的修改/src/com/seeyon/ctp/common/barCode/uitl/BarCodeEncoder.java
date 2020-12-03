package com.seeyon.ctp.common.barCode.uitl;

import com.seeyon.ctp.util.LightWeightEncoder;
import com.seeyon.ctp.util.Strings;
import www.seeyon.com.utils.DesUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 二维码正文加解密
 * Created by daiye on 2016-1-10.
 */
public class BarCodeEncoder {
    
    private Map<String, BarEncodeType> encodeMap = new HashMap<String, BarEncodeType>();
    private Map<String, BarEncodeType> preEncodeMap = new HashMap<String, BarEncodeType>();

    private static BarCodeEncoder encoder = new BarCodeEncoder();
    
    private BarCodeEncoder () {
        registerEncodeType();
    }

    public static BarCodeEncoder getInstance() {
        return encoder;
    }

    /**
     * 注册加解密类型
     */
    private void registerEncodeType() {

        NoEncodeType noEncodeType = new NoEncodeType();
        encodeMap.put(noEncodeType.getType(), noEncodeType);
        preEncodeMap.put(noEncodeType.getPre(), noEncodeType);

        NormalEncodeType normalEncodeType = new NormalEncodeType();
        encodeMap.put(normalEncodeType.getType(), normalEncodeType);
        preEncodeMap.put(normalEncodeType.getPre(), normalEncodeType);

        HighEncodeType highEncodeType = new HighEncodeType();
        encodeMap.put(highEncodeType.getType(), highEncodeType);
        preEncodeMap.put(highEncodeType.getPre(), highEncodeType);
    }

    /**
     * 加密字符串
     * @param content 需要加密的内容
     * @param coderLevel 加密级别
     * @return 加密后的结果
     */
    public String encode(String content, String coderLevel) throws Exception {
        BarEnums.EncodeLevel level = BarEnums.EncodeLevel.getEnumByKey(coderLevel);
        return encode(content, level);
    }

    /**
     * 加密字符串
     * @param content 需要加密的内容
     * @param level 加密级别
     * @return 加密后的结果
     */
    public String encode(String content, BarEnums.EncodeLevel level) throws Exception {
        if (Strings.isBlank(content)) {
            return content;
        }
        BarEncodeType type = encodeMap.get(level.getKey());
        return type.encode(content);
    }

    /**
     * 解密字符串
     * @param content 需要解密的串
     * @return 解密后的字符串
     * @throws Exception
     */
    public String decode(String content) throws Exception {
        if(Strings.isBlank(content) || content.length() < BarEncodeType.PRE_LENGTH) {
            return content;
        }
        String pre = content.substring(0, BarEncodeType.PRE_LENGTH);
        BarEncodeType type = preEncodeMap.get(pre);
        if (type == null) {
            return content;
        }
        String str = content.substring(BarEncodeType.PRE_LENGTH);
        return type.decode(str);
    }

    /**
     * 加密类型
     */
    interface BarEncodeType{

        int PRE_LENGTH = 5;

        /**
         * 返回加密类型
         * @return 加密类型
         */
        String getType();

        /**
         * 加密后的字符串前缀
         *
         * 供解密时提供
         * @return 前缀
         */
        String getPre();

        /**
         * 加密
         * @param content 被加密内容
         * @return 加密结果
         */
        String encode(String content) throws Exception;

        /**
         * 解密
         * @param content 被解密内容
         * @return 解密后的结果
         */
        String decode(String content) throws Exception;
    }

    class NoEncodeType implements BarEncodeType {

        @Override
        public String getType() {
            return BarEnums.EncodeLevel.NO.getKey();
        }

        @Override
        public String getPre() {
            return "/1.0/";
        }

        @Override
        public String encode(String content) {
            return getPre() + content;
        }

        @Override
        public String decode(String content) {
            return content;
        }
    }

    class NormalEncodeType implements BarEncodeType {

        @Override
        public String getType() {
            return BarEnums.EncodeLevel.NORMAL.getKey();
        }

        @Override
        public String getPre() {
            return "/2.0/";
        }

        @Override
        public String encode(String content) {
            return getPre() + LightWeightEncoder.encodeString(content);
        }

        @Override
        public String decode(String content) {
            return LightWeightEncoder.decodeString(content);
        }
    }

    class HighEncodeType implements BarEncodeType {

        private static final String key = "wqp9348yet[qw0-";

        @Override
        public String getType() {
            return BarEnums.EncodeLevel.HIGH.getKey();
        }

        @Override
        public String getPre() {
            return "/3.0/";
        }

        @Override
        public String encode(String content) throws Exception {
            return getPre() + DesUtil.encode(content, key);
        }

        @Override
        public String decode(String content) throws Exception {
            return DesUtil.decode(content, key);
        }
    }
}
