/**
 * $Author: libing $
 * $Rev: 79431 $
 * $Date:: 2015-03-05 16:40:39#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.apps.collaboration.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.manager.ColManager;
import com.seeyon.apps.collaboration.manager.ColMessageManager;
import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.AbstractContentInterface;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.common.usermessage.MessageContent;
import com.seeyon.ctp.common.usermessage.MessageReceiver;
import com.seeyon.ctp.common.usermessage.UserMessageManager;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;

/**
 * <p>Title: F1</p>
 * <p>Description: 内容组件协同应用自定义扩展逻辑接口定义</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 */
public class ColContentInterfaceImpl extends AbstractContentInterface {
    private static Log LOG = CtpLogFactory.getLog(ColContentInterfaceImpl.class);
    private ColMessageManager colMessageManager = (ColMessageManager) AppContext.getBean("colMessageManager");
    private OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
    private UserMessageManager userMessageManager = (UserMessageManager) AppContext.getBean("userMessageManager");
    private ColManager colManager = (ColManager)AppContext.getBean("colManager");
    @Override
    public void doCommentPushMessage(Comment comment) throws BusinessException {
        if(Integer.valueOf(Comment.CommentType.sender.getKey()).equals(comment.getCtype())){
        	if(comment.isPushMessage() != null && comment.isPushMessage()){
        		colMessageManager.doCommentPushMessage4SenderNote(comment);
        	}
        }else{
            colMessageManager.doCommentPushMessage4Reply(comment);
        }
    }

	@Override
	public void doCommentPrise(User sender, String subject,
			CtpCommentAll c) throws BusinessException {
		V3xOrgMember _member;
        try {
          ColSummary summaryById = colManager.getSummaryById(Long.parseLong(subject));
          String _title = summaryById.getSubject();
          _member = orgManager.getMemberById(c.getCreateId());
          MessageContent mc = new MessageContent("collaboration.opinion.deal.praise",sender.getName(), _title.replaceAll("&nbsp;", " "), _member.getName());
          if (null != summaryById.getTempleteId()) {
        	  mc.setTemplateId(summaryById.getTempleteId());
          }
          Set<MessageReceiver> receivers = new HashSet<MessageReceiver>();
          MessageReceiver  mr = new MessageReceiver(c.getAffairId(), c.getCreateId(),"message.link.col.pending", c.getAffairId(), c.getId());
          mr.setReply(true);
          receivers.add(mr);
          userMessageManager.sendSystemMessage(mc, ApplicationCategoryEnum.collaboration,sender.getId(),receivers);
        } catch(BusinessException e){
        	LOG.error(e.getLocalizedMessage(),e);
        }
	}
}
