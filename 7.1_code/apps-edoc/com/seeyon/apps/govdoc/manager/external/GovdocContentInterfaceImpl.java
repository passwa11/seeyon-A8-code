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
package com.seeyon.apps.govdoc.manager.external;

import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.manager.GovdocMessageManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.vo.GovdocBaseVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.content.AbstractContentInterface;
import com.seeyon.ctp.common.content.comment.Comment;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.comment.CtpCommentAll;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.v3x.edoc.domain.EdocSummary;

/**
 * <p>
 * Title: F1
 * </p>
 * <p>
 * Description: 内容组件公文应用自定义扩展逻辑接口定义
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: seeyon.com
 * </p>
 */
public class GovdocContentInterfaceImpl extends AbstractContentInterface {
		
	private GovdocSummaryManager govdocSummaryManager = (GovdocSummaryManager) AppContext.getBean("govdocSummaryManager");
	private GovdocMessageManager govdocMessageManager = (GovdocMessageManager) AppContext.getBean("govdocMessageManager");
	
	@Override
	public void doCommentPushMessage(Comment comment) throws BusinessException {
		if (Integer.valueOf(Comment.CommentType.sender.getKey()).equals(comment.getCtype())) {
			if (comment.isPushMessage() != null && comment.isPushMessage()) {
				govdocMessageManager.sendPushMessage4SenderNote(comment);
			}
		} else {
			govdocMessageManager.sendPushMessage4Reply(comment);
		}
	}

	@Override
	public void doCommentPrise(User sender, String summaryId, CtpCommentAll c) throws BusinessException {
		EdocSummary summary = govdocSummaryManager.getSummaryById(Long.parseLong(summaryId));
		GovdocBaseVO baseVo = new GovdocBaseVO();
		baseVo.setSummary(summary);
		baseVo.setMember(GovdocOrgHelper.getEntityById(V3xOrgMember.class, c.getCreateId()));
		baseVo.setCurrentUser(sender);
		baseVo.setCommentAll(c);
		govdocMessageManager.sendCommentAddPraiseMsg(baseVo);
	}
	
}
