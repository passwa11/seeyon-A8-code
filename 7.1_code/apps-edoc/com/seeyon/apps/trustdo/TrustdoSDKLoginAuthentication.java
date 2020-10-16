package com.seeyon.apps.trustdo;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.seeyon.apps.trustdo.model.sdk.BizAccountData;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.logging.Log;

import com.google.common.base.Strings;
import com.seeyon.apps.trustdo.constants.SDKConstants;
import com.seeyon.apps.trustdo.constants.TrustdoErrorMsg;
import com.seeyon.apps.trustdo.constants.XRDPhoneConstants;
import com.seeyon.apps.trustdo.exceptions.XRDUserException;
import com.seeyon.apps.trustdo.model.sdk.LoginData;
import com.seeyon.apps.trustdo.model.sdk.Result;
import com.seeyon.apps.trustdo.utils.XRDAppUtils;
import com.seeyon.apps.trustdo.utils.XRDHttpUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.login.AbstractLoginAuthentication;
import com.seeyon.ctp.login.LoginAuthenticationException;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.bo.V3xOrgPrincipal;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.json.JSONUtil;

/**
 * A8系统手机盾插件扫码登录拦截器
 *
 * @author zhaopeng
 */
public class TrustdoSDKLoginAuthentication extends AbstractLoginAuthentication {

    private static final Log LOGGER = CtpLogFactory.getLog(TrustdoSDKLoginAuthentication.class);

    private OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");

    @Override
    public String[] authenticate(HttpServletRequest request,
                                 HttpServletResponse response) throws LoginAuthenticationException {
        //用户账号
        String login_username = request.getParameter("login_username");
        //手机盾登录标识trustdo_type = "true"
        String trustdo_type = request.getParameter("trustdo_type");
        if (!Strings.isNullOrEmpty(trustdo_type)) {
            trustdo_type = trustdo_type.contains("\"") ? trustdo_type.replaceAll("\"", "") : trustdo_type;
        }
        //SDK登录票据
        String accToken = request.getParameter("accToken");
        if (!Strings.isNullOrEmpty(accToken)) {
            accToken = accToken.contains("\"") ? accToken.replaceAll("\"", "") : accToken;
        }
        LOGGER.debug(System.currentTimeMillis() + ">>>>>>>Trustdo LoginAuthenticate login_username is : " + login_username);
        LOGGER.debug(System.currentTimeMillis() + ">>>>>>>Trustdo LoginAuthenticate trustdo_type is : " + trustdo_type);
        LOGGER.debug(System.currentTimeMillis() + ">>>>>>>Trustdo LoginAuthenticate accToken is : " + accToken);

        String appKeySign = null;
        if ("mokey_m3".equals(trustdo_type) && !Strings.isNullOrEmpty(login_username) && !Strings.isNullOrEmpty(accToken)) {
            LOGGER.debug(System.currentTimeMillis() + ">>>>>>>TrustdoM3 LoginAuthenticate>>>>>>>");
            Map<String, Object> signMap = new HashMap<String, Object>();
            signMap.put("appKey", XRDPhoneConstants.APP_KEY);
            signMap.put("accToken", accToken);
            try {
                appKeySign = XRDAppUtils.getMd5Sign(signMap, XRDPhoneConstants.APP_SECRET);
            } catch (XRDUserException e1) {
                LOGGER.debug("TrustdoM3 LoginAuthenticate getSDKLoginName getMd5Sign Error!");
            }
            String url = SDKConstants.SDK_GET_TOKEN_URL;
            NameValuePair[] data = {
                    new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
                    new NameValuePair("accToken", accToken),
                    new NameValuePair("sign", appKeySign)};

            Result<?> loginResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, LoginData.class);
            if (loginResult != null && loginResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
                LOGGER.debug("TrustdoM3 LoginAuthenticate loginResult is:" + JSONUtil.toJSONString(loginResult));
                if (JSONUtil.toJSONString(loginResult).contains(login_username)) {
                    V3xOrgMember orgMember = null;
                    try {
                        orgMember = this.orgManager.getMemberByLoginName(login_username);
                    } catch (BusinessException e) {
                        LOGGER.error(e);
                        //e.printStackTrace();
                    }
                    if (orgMember != null) {
                        V3xOrgPrincipal orgPrincipal = orgMember.getV3xOrgPrincipal();
                        String loginName = orgPrincipal.getLoginName();
                        String passWord = orgPrincipal.getPassword();
                        if (loginName != null && !"".equals(loginName) && passWord != null && !"".equals(passWord)) {
                            return new String[]{loginName, passWord};
                        }
                    }
                } else {
                    LOGGER.debug("TrustdoM3 LoginAuthenticate loginName compare bizAccount failed!");
                    return null;
                }
            } else {
                LOGGER.debug("TrustdoM3 LoginAuthenticate sdkConnectMobileShieldServer failed！");
                return null;
            }
        }
        if ("mokey_pc".equals(trustdo_type) && !Strings.isNullOrEmpty(login_username)) {
            LOGGER.debug(System.currentTimeMillis() + ">>>>>>>TrustdoA8 LoginAuthenticate>>>>>>>");
            V3xOrgMember orgMember = null;
            String url = SDKConstants.SDK_GET_TOKEN_URL;
            NameValuePair[] data = {
                    new NameValuePair("appKey", XRDPhoneConstants.APP_KEY),
                    new NameValuePair("accToken", login_username),
                    new NameValuePair("sign", appKeySign)};

            Result<?> loginResult = XRDHttpUtils.sdkConnectMobileShieldServer(data, url, LoginData.class);
            if (loginResult != null && loginResult.getErrCode().equals(TrustdoErrorMsg.SUCCESS_CODE)) {
                LOGGER.debug("getBizLoginName loginResult:" + JSONUtil.toJSONString(loginResult));
                BizAccountData bizAccountData = JSONUtil.parseJSONString(JSONUtil.toJSONString(loginResult.getData()), BizAccountData.class);
                try {
                    orgMember = this.orgManager.getMemberByLoginName(bizAccountData.getzAccount());
                } catch (BusinessException e) {
                    LOGGER.error(e);
                }
                if (orgMember != null) {
                    V3xOrgPrincipal orgPrincipal = orgMember.getV3xOrgPrincipal();
                    String loginName = orgPrincipal.getLoginName();
                    String passWord = orgPrincipal.getPassword();
                    if (loginName != null && !"".equals(loginName) && passWord != null && !"".equals(passWord)) {
                        return new String[]{loginName, passWord};
                    }
                } else {
                    return null;
                }
            }
        }
        return null;
    }

}
