package com.seeyon.ctp.common.barCode.manager;

import com.seeyon.ctp.common.exceptions.BusinessException;

import java.sql.SQLException;
import java.util.Map;

/**
 * 二维码类型manager
 * 根据不同的类型定义不同的数据源和数据解析
 * Created by daiye on 2016-1-4.
 */
public interface BarCodeTypeManager {

    /**
     * 默认解析类
     */
    String BASE_BAR_CODE_TYPE_KEY = "default";

    /**
     * 返回当前实现服务的类型
     * @return 类型
     */
    String getType();

    /**
     * 根据传入的参数获取需要转换为二维码的字符串
     * @param param 参数，自定义的参数
     * @return 需要转换的字符串
     */
    String getContentStr(Map<String, Object> param);

    /**
     * 当生成的二维码内容超过长度之后，生成自己想要的特殊二维码
     * @param param
     * @return
     */
    String getContent4OutOfLength(Map<String,Object> param);

    /**
     * 解析从二维码中获取的数据
     * @param decodeStr 二维码中数据
     * @param param 解析参数
     * @return 解析结果
     */
    Object decode(String decodeStr, Map<String, Object> param) throws SQLException, BusinessException;
}
