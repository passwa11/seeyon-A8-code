package com.seeyon.ctp.common.barCode.manager.impl;

import com.seeyon.ctp.common.barCode.manager.BarCodeTypeManager;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.json.JSONUtil;
import org.apache.commons.logging.Log;

import java.util.Map;

/**
 * 默认实现类
 * Created by daiye on 2016-1-4.
 */
public class BaseBarCodeTypeManagerImpl implements BarCodeTypeManager {

    private static Log log = CtpLogFactory.getLog(BaseBarCodeTypeManagerImpl.class);

    @Override
    public String getType() {
        return BASE_BAR_CODE_TYPE_KEY;
    }

    @Override
    public String getContentStr(Map<String, Object> param) {
        return JSONUtil.toJSONString(param);
    }

    /**
     * 当生成的二维码内容超过长度之后，生成自己想要的特殊二维码
     *
     * @param param
     * @return
     */
    @Override
    public String getContent4OutOfLength(Map<String, Object> param) {
        return JSONUtil.toJSONString(param);
    }

    @Override
    public Object decode(String decodeStr, Map<String, Object> param) {
        Object json;
        try {
            json = JSONUtil.parseJSONString(decodeStr);
        } catch (Exception e) {
            log.error("默认实现json解析二维码数据异常，直接返回结果值：" + decodeStr, e);
            return decodeStr;
        }
        return json;
    }
}
