package com.seeyon.ctp.rest.resources.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.content.comment.Comment.CommentType;
import com.seeyon.ctp.common.content.comment.CommentManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.rest.resources.ColSummaryCommentVO;
import com.seeyon.ctp.util.Strings;

/**
 * 转发附言VO
 */
public class ColForwordCommentVO {
    private int                       zanCount     = 0;                                   //赞计数
    private Integer                   forwardCount;                                       //第X次转发
    private List<ColSummaryCommentVO> noteList   = new ArrayList<ColSummaryCommentVO>(); //附言列表
    private List<ColSummaryCommentVO> replysList = new ArrayList<ColSummaryCommentVO>();

    public ColForwordCommentVO(int forwardCount) {
        this.forwardCount = forwardCount;
    }

    public static List<ColForwordCommentVO> valueOf(List<Comment> commentListAll, CommentManager commentManager) throws BusinessException {
        List<ColForwordCommentVO> voList = new ArrayList<ColForwordCommentVO>();
        Map<Integer, ColForwordCommentVO> groupComment = new TreeMap<Integer, ColForwordCommentVO>();
        if (commentListAll != null) {
            List<ColSummaryCommentVO> commentList = new ArrayList<ColSummaryCommentVO>();
            if (Strings.isNotEmpty(commentListAll)) {//获取子回复
                List<Long> commentIds = new ArrayList<Long>();
                for (Comment c : commentListAll) {
                    commentIds.add(c.getId());
                }
                Map<Long, List<Comment>> subComments = commentManager.findCommentReplay(commentIds);
                for (Comment c : commentListAll) {
                    ColSummaryCommentVO vo = ColSummaryCommentVO.valueOf(c);//子回复
                    List<Comment> subReplysList = subComments.get(c.getId());
                    vo.setSubReplys(ColSummaryCommentVO.valueOf(subReplysList));
                    commentList.add(vo);
                }
            }

            for (ColSummaryCommentVO vo : commentList) {
                Integer forwordCount = vo.getForwardCount();
                if (forwordCount != null) {
                    ColForwordCommentVO forwordComment = groupComment.get(forwordCount);
                    if (forwordComment == null) {
                        forwordComment = new ColForwordCommentVO(forwordCount);
                        groupComment.put(forwordCount, forwordComment);
                    }
                    forwordComment.addComment(vo);
                }
            }

            for (Integer forwordCount : groupComment.keySet()) {
                voList.add(groupComment.get(forwordCount));
            }
        }
        return voList;
    }

    public void addComment(ColSummaryCommentVO comment) {
        Integer ctype = comment.getCtype();
        if (ctype == CommentType.sender.getKey()) {//附言
            this.noteList.add(comment);
        } else if (ctype == CommentType.comment.getKey()) {//回复
            if (comment.getPraiseToSummary()) {
                countZan(1);
            }
            this.replysList.add(comment);
        }
    }

    public int getZanCount() {
        return zanCount;
    }

    public void countZan(Integer zan) {
        if (zan != null) {
            zanCount += zan;
        }
    }

    public Integer getForwardCount() {
        return forwardCount;
    }

    public List<ColSummaryCommentVO> getNoteList() {
        return noteList;
    }

    public List<ColSummaryCommentVO> getReplysList() {
        return replysList;
    }
}