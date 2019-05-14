package com.seeyon.apps.testBPM.po;

/**
 * @author Fangaowei
 * 
 *         <pre>
 *         </pre>
 * 
 * @date 2018年9月5日 上午10:00:28 @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class Response {

    private int returnCode;

    private String returnMsg;

    private String dataId;

    private String message;

    public Response() {
    }

    public Response(int code, String msg) {
        this.returnCode = code;
        this.returnMsg = msg;
        this.message = returnMsg;
    }

    /**
     * 处理成功
     * @param msg
     * @return
     */
    public Response success(String msg) {
        this.returnCode = 1;
        this.returnMsg = msg;
        this.message = returnMsg;
        return this;
    }

    /**
     * 回退
     * @param msg
     * @return
     */
    public Response back(String msg) {
        this.returnCode = 2;
        this.returnMsg = msg;
        this.message = returnMsg;
        return this;
    }

    /**
     * 暂存待办
     * @param msg
     * @return
     */
    public Response zcdb(String msg) {
        this.returnCode = 0;
        this.returnMsg = msg;
        this.message = returnMsg;
        return this;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
