package com.seeyon.v3x.edoc.event;

import com.seeyon.ctp.event.Event;

/**
 * 保存公文模版事件
 * @author chenx
 *
 */
public class EdocTemplateSaveEvent extends Event {

	public EdocTemplateSaveEvent(Object source) {
		super(source);
	}
	
	private Long templateId;

	public Long getTemplateId() {
		return templateId;
	}

	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -47347135408542410L;

}
