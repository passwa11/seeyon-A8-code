/**
 * 
 */
package com.seeyon.v3x.edoc.webmodel;

/** 
* @Description: TODO
* @author muj
* @date 2018年6月4日 下午3:54:59 
*  
*/
public class MessageCommentParam {
    
    //意见ID
    private long opinionId;
    //是否隐藏
    private Boolean isHidden;
    //意见内容
    private String opinion;
    //态度
    private int attitude;
    //是否上传附件
    private boolean isUploadAtt;
    
    public MessageCommentParam(){
    }

    public MessageCommentParam(long opinionId,Boolean isHidden,String opinion, int attitude,boolean isUploadAtt){
        this.opinionId = opinionId;
        this.isHidden = isHidden;
        this.opinion = opinion;
        this.attitude = attitude;
        this.isUploadAtt = isUploadAtt;
    }
    
    public long getOpinionId() {
        return opinionId;
    }
    public void setOpinionId(long opinionId) {
        this.opinionId = opinionId;
    }
    public Boolean getIsHidden() {
        return isHidden;
    }
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
    public String getOpinion() {
        return opinion;
    }
    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
    public int getAttitude() {
        return attitude;
    }
    public void setAttitude(int attitude) {
        this.attitude = attitude;
    }
    public boolean isUploadAtt() {
        return isUploadAtt;
    }
    public void setUploadAtt(boolean isUploadAtt) {
        this.isUploadAtt = isUploadAtt;
    }
    
}
