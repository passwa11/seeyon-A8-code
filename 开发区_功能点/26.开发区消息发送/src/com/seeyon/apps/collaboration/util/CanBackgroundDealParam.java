/**
 * 
 */
package com.seeyon.apps.collaboration.util;

import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.workflow.wapi.PopResult;

/** 
* @Description: TODO
* @author muj
* @date 2018年4月21日 上午9:58:11 
*  
*/
public class CanBackgroundDealParam {
    private CtpAffair affair;
    private PopResult pr;
    private Comment comment;//处理意见
    
    
    public PopResult getPr() {
        return pr;
    }
    public void setPr(PopResult pr) {
        this.pr = pr;
    }
    public CtpAffair getAffair() {
        return affair;
    }
    public void setAffair(CtpAffair affair) {
        this.affair = affair;
    }
    public Comment getComment() {
		return comment;
	}
    public void setComment(Comment comment) {
		this.comment = comment;
	}
}
