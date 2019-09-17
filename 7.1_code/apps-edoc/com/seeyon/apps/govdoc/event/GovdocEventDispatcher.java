package com.seeyon.apps.govdoc.event;

import java.util.Map;

import com.seeyon.apps.collaboration.event.CollaborationCancelEvent;
import com.seeyon.apps.govdoc.util.GovdocParamUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.event.EventDispatcher;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class GovdocEventDispatcher {
	
	/**
	 * 公文流程撤销相关事件调用
	 * @param source
	 * @param paramMap
	 */
	public static void fireEventForCancel(Object source, Map<String, Object> paramMap) {
		// 撤销流程事件
		CollaborationCancelEvent event = new CollaborationCancelEvent(source);
		event.setSummaryId(GovdocParamUtil.getLong(paramMap, "summaryId"));
		event.setUserId(GovdocParamUtil.getLong(paramMap, "userId"));
		event.setMessage(GovdocParamUtil.getString(paramMap, "repealComment"));
		EventDispatcher.fireEvent(event);
		// 发送消息给督办人，更新督办状态，并删除督办日志、删除督办记录、删除催办次数
	}
	
	/**
	 * 公文处理-多级会签
	 * @param source
	 * @param summary
	 * @param affair
	 */
	public static void fireEventForCurJsonPerm(Object source, EdocSummary summary, CtpAffair affair) {
		GovDocURLEvent event = new GovDocURLEvent(source);
		if (affair != null) {
			event.setAffairId(affair.getId());
		}
		event.setEdocSummary(summary);
		event.setApp(String.valueOf(ApplicationCategoryEnum.edoc.key()));
		event.setSubApp(String.valueOf(summary.getGovdocType()));
		EventDispatcher.fireEvent(event);
	}
	
	
}
