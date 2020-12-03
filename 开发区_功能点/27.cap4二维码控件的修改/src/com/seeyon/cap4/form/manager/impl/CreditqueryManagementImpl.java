package com.seeyon.cap4.form.manager.impl;

import com.seeyon.apps.mplus.api.MplusApi;
import com.seeyon.cap4.form.manager.CreditqueryManager;
import com.seeyon.cap4.template.util.HttpClientUtil;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.util.annotation.AjaxAccess;
import org.apache.commons.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreditqueryManagementImpl implements CreditqueryManager  {
    private static final Log LOGGER = CtpLogFactory.getLog(CreditqueryManagementImpl.class);
    final String serviceCode = "m20000000000003002";

    /**
     * 获取企业征信的分页信息
     * @param fi
     * @param params
     * @return
     * @throws BusinessException
     */
    @Override
    @AjaxAccess
    public FlipInfo creditqueryPageing(FlipInfo fi, Map<String, Object> params) throws BusinessException {
        String keyword = (String)params.get("keyword");
        if(Strings.isNotBlank(keyword)){
            try {
                Map<String, Object> jsonObject = crditeInfotBykeywordPost( keyword, fi.getPage());
                String total = (String) jsonObject.get("total");
                List<Map<String, Object>> map_list = (List<Map<String, Object>>) jsonObject.get("items");
                int t = Integer.valueOf(total);
                if(t>200){
                    //第三方只能返回最多200条数据，所以这个地方特殊处理
                    fi.setTotal(200);
                }else{
                    fi.setTotal(t);
                }
                fi.setData(map_list);
            } catch (BusinessException e) {
                LOGGER.error(e.getMessage(), e);
                throw  e;
            }
        }


        return fi;
    }


    /**
     * 模糊查询 企业名称关键字/注册号/统一社会信用代码 其他：公司全名或企业注册号；
     * @param keyword
     * @param pageNum
     * @return
     * @throws BusinessException
     */
    Map<String, Object> crditeInfotBykeywordPost(String keyword, Integer pageNum) throws BusinessException {
        MplusApi mplusApi = (MplusApi) AppContext.getBean("mplusApi");
        String domain = mplusApi.getDomain();
        String ticket = mplusApi.getTicket(serviceCode);
        String url = domain + "/svr/enterprise/info";
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("ticket", ticket);
        map.put("keyword", keyword.replace(" ", ""));
        map.put("type", "2");
        map.put("pageNum", pageNum);
        map.put("parameterType", "2");
        Map<String, Object> res = HttpClientUtil.doPost(url, map);
        String state = res.get("code").toString();
        if (!"1000".equals(state)) {
            throw new BusinessException(res.get("msg").toString());
        }
        return (Map<String, Object>) res.get("data");
    }
}
