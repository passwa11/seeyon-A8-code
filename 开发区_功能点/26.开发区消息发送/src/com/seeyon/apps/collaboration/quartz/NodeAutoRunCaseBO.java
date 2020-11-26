package com.seeyon.apps.collaboration.quartz;

import com.seeyon.ctp.workflow.wapi.PopResult;

public class NodeAutoRunCaseBO {
    private NodeOverTimeAutoRunCheckCode code;
    private String matchRequestToken;
    private String isStrongValidate;
    private PopResult pr;

    public PopResult getPr() {
        return pr;
    }

    public void setPr(PopResult pr) {
        this.pr = pr;
    }

    public String getIsStrongValidate() {
        return isStrongValidate;
    }

    public void setIsStrongValidate(String isStrongValidate) {
        this.isStrongValidate = isStrongValidate;
    }

    public NodeOverTimeAutoRunCheckCode getCode() {
        return code;
    }

    public void setCode(NodeOverTimeAutoRunCheckCode code) {
        this.code = code;
    }

    public String getMatchRequestToken() {
        return matchRequestToken;
    }

    public void setMatchRequestToken(String matchRequestToken) {
        this.matchRequestToken = matchRequestToken;
    }
}
