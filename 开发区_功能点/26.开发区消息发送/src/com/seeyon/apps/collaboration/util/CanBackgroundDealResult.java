/**
 * 
 */
package com.seeyon.apps.collaboration.util;

/** 
* @Description: 超期跳过、重复跳过、批处理、AI处理共有的能否处理的校验结果类
* @author muj
* @date 2018年4月21日 上午9:49:38 
*  
*/
public class CanBackgroundDealResult {
    private boolean can = true;
    private String  msg ;
    private String branchArgs; //流程分支数据
    public boolean isCan() {
        return can;
    }
    public void setCan(boolean can) {
        this.can = can;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getBranchArgs() {
        return branchArgs;
    }
    public void setBranchArgs(String branchArgs) {
        this.branchArgs = branchArgs;
    } 
}