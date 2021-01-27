package com.seeyon.ctp.common.barCode.vo;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.seeyon.ctp.common.barCode.manager.BarCodeTypeManager;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.util.ParamUtil;
import com.seeyon.ctp.util.Strings;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 二维码生成参数 vo对象
 * Created by daiyi on 2016-1-6.
 */
public class BarCodeParamVo {

    private String bj;
    /**
     * 二维码logo
     * 默认无
     */
    private String logoPath;

    /**
     * 二维码logo宽度
     * 默认无
     */
    private int logoWidth = 68;

    /**
     * 二维码logo高度
     * 默认无
     */
    private int logoHeight = 68;

    /**
     * 二维码宽度
     * <p/>
     * 默认300
     */
    private int width;

    /**
     * 高度
     * 默认300
     */
    private int height;

    /**
     * 生成文件扩展名
     * <p/>
     * 默认png
     */
    private String fileExt;

    /**
     * 生成的二维码格式
     * <p/>
     * 默认QR_CODE
     *
     * @see BarcodeFormat
     */
    private BarcodeFormat barcodeFormat;

    /**
     * 容错级别
     * <p/>
     * 默认 L
     *
     * @see ErrorCorrectionLevel
     */
    private ErrorCorrectionLevel errorLevel;

    /**
     * 指定的编码格式
     */
    private String charset;

    /**
     * 二维码生成，解析类型
     *
     * @see BarCodeTypeManager;
     */
    private String codeType;

    /**
     * 二维码生成所属分类
     */
    private ApplicationCategoryEnum categoryEnum;

    /**
     * 一级应用
     */
    private Long reference;

    /**
     * 二级应用
     */
    private Long subReference;

    /**
     * 加密级别
     */
    private String encodeVersion;

    /**
     * 生成二维码长度超长之后，是否抛出异常
     */
    private boolean throwException;

    /**
     * 该二维码支持的内容最大长度，根据不同的应用可以自定义这个长度，默认1000
     */
    private int maxLength;

    public BarCodeParamVo() {
        init(new HashMap<String, Object>());
    }

    public BarCodeParamVo(Map<String, Object> param) {
        init(param);
    }

    public void init(Map<String, Object> param) {
        setWidth(ParamUtil.getInt(param, PARAM_KEY_WIDTH, PARAM_KEY_WIDTH_DEFAULT));
        setHeight(ParamUtil.getInt(param, PARAM_KEY_HEIGHT, PARAM_KEY_HEIGHT_DEFAULT));
        setFileExt(ParamUtil.getString(param, PARAM_KEY_FILE_EXT, PARAM_KEY_FILE_EXT_DEFAULT));
        setBarcodeFormat(ParamUtil.getString(param, PARAM_KEY_BARCODE_FORMAT, PARAM_KEY_BARCODE_FORMAT_DEFAULT));
        setErrorLevel(ParamUtil.getString(param, PARAM_KEY_ERROR_LEVEL, PARAM_KEY_ERROR_LEVEL_DEFAULT));
        setCharset(ParamUtil.getString(param, PARAM_KEY_CHARSET, PARAM_KEY_CHARSET_DEFAULT));
        setCodeType(ParamUtil.getString(param, PARAM_KEY_CODE_TYPE, PARAM_KEY_CODE_TYPE_DEFAULT));
        setCategory(ParamUtil.getInt(param, PARAM_KEY_CATEGORY_ENUM, PARAM_KEY_CATEGORY_ENUM_DEFAULT));
        setReference(ParamUtil.getLong(param, PARAM_KEY_REFERENCE, PARAM_KEY_REFERENCE_DEFAULT));
        setSubReference(ParamUtil.getLong(param, PARAM_KEY_SUB_REFERENCE, PARAM_KEY_SUB_REFERENCE_DEFAULT));
        setEncodeLevel(ParamUtil.getString(param, PARAM_KEY_ENCODE_LEVEL, PARAM_KEY_ENCODE_LEVEL_DEFAULT));
        //只要不是false，就设置为true
        setThrowException("false".equals(ParamUtil.getString(param, PARAM_KEY_THROW_EXCEPTION)) ? false : true);
        setMaxLength(ParamUtil.getInt(param, PARAM_KEY_MAX_LENGTH, PARAM_KEY_MAX_LENGTH_DEFAULT));
        //zhou
        setBj(ParamUtil.getString(param,BJ));
    }

    public Map<EncodeHintType, ?> getHintParam() {
        Map<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, getCharset());
        hints.put(EncodeHintType.ERROR_CORRECTION, getErrorLevel());
        hints.put(EncodeHintType.MARGIN, 1);
        return hints;
    }


    private static final String PARAM_KEY_WIDTH = "width";
    private static final int PARAM_KEY_WIDTH_DEFAULT = 300;
    private static final String PARAM_KEY_HEIGHT = "height";
    private static final int PARAM_KEY_HEIGHT_DEFAULT = 300;
    private static final String PARAM_KEY_FILE_EXT = "fileExt";
    private static final String PARAM_KEY_FILE_EXT_DEFAULT = "png";
    private static final String PARAM_KEY_BARCODE_FORMAT = "barcodeFormat";
    private static final String PARAM_KEY_BARCODE_FORMAT_DEFAULT = "QR_CODE";
    private static final String PARAM_KEY_ERROR_LEVEL = "errorLevel";
    private static final String PARAM_KEY_ERROR_LEVEL_DEFAULT = "L";
    private static final String PARAM_KEY_CHARSET = "charset";
    private static final String PARAM_KEY_CHARSET_DEFAULT = "utf-8";
    private static final String PARAM_KEY_CODE_TYPE = "codeType";
    private static final String PARAM_KEY_CODE_TYPE_DEFAULT = BarCodeTypeManager.BASE_BAR_CODE_TYPE_KEY;
    private static final String PARAM_KEY_CATEGORY_ENUM = "category";
    private static final int PARAM_KEY_CATEGORY_ENUM_DEFAULT = 0;
    private static final String PARAM_KEY_REFERENCE = "reference";
    private static final Long PARAM_KEY_REFERENCE_DEFAULT = 0L;
    private static final String PARAM_KEY_SUB_REFERENCE = "subReference";
    private static final Long PARAM_KEY_SUB_REFERENCE_DEFAULT = 0L;
    private static final String PARAM_KEY_ENCODE_LEVEL = "encodeLevel";
    private static final String PARAM_KEY_ENCODE_LEVEL_DEFAULT = "no";
    private static final String PARAM_KEY_THROW_EXCEPTION = "throwException";
    private static final String PARAM_KEY_MAX_LENGTH = "maxLength";
    private static final int PARAM_KEY_MAX_LENGTH_DEFAULT = 1000;
    private static final String BJ = "false";

    public String getBj() {
        return bj;
    }

    public void setBj(String bj) {
        this.bj = bj;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public int getLogoWidth() {
        return logoWidth;
    }

    public void setLogoWidth(int logoWidth) {
        this.logoWidth = logoWidth;
    }

    public int getLogoHeight() {
        return logoHeight;
    }

    public void setLogoHeight(int logoHeight) {
        this.logoHeight = logoHeight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public BarcodeFormat getBarcodeFormat() {
        return barcodeFormat;
    }

    public void setBarcodeFormat(String barcodeFormat) {
        if (Strings.isNotBlank(barcodeFormat)) {
            this.barcodeFormat = BarcodeFormat.valueOf(barcodeFormat);
        }
        if (this.barcodeFormat == null) {
            this.barcodeFormat = BarcodeFormat.QR_CODE;
        }
    }

    public Object getErrorLevel() {
        Object result = errorLevel;
        if (barcodeFormat == BarcodeFormat.PDF_417) {
            result = errorLevel.ordinal() * 2;
        }
        return result;
    }

    public void setErrorLevel(String errorLevel) {
        if (Strings.isNotBlank(errorLevel)) {
            this.errorLevel = ErrorCorrectionLevel.valueOf(errorLevel);
        }
        if (this.errorLevel == null) {
            this.errorLevel = ErrorCorrectionLevel.L;
        }
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    public ApplicationCategoryEnum getCategoryEnum() {
        return categoryEnum;
    }

    public void setCategory(int category) {
        this.categoryEnum = ApplicationCategoryEnum.valueOf(category);
        if (categoryEnum == null) {
            this.categoryEnum = ApplicationCategoryEnum.global;
        }
    }

    public Long getReference() {
        return reference;
    }

    public void setReference(Long reference) {
        this.reference = reference;
    }

    public Long getSubReference() {
        return subReference;
    }

    public void setSubReference(Long subReference) {
        this.subReference = subReference;
    }

    public String getEncodeVersion() {
        return encodeVersion;
    }

    public void setEncodeLevel(String encodeVersion) {
        this.encodeVersion = encodeVersion;
    }

    public boolean isThrowException() {
        return throwException;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
