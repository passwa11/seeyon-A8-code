package com.seeyon.ctp.common.filemanager.event;

import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.event.Event;

/**
 * 附件保存事件。附件生成并持久化以后触发。
 * 
 * @author wangwenyou
 * 
 */
public class AttachmentSaveEvent extends Event {
    /**
     * 
     */
    private static final long serialVersionUID = 6305690995207240052L;
    private final Attachment  attachment;

    /**
     * 取得所保存的附件实体。
     * 
     * @return 附件实体对象
     */
    public Attachment getAttachment() {
        return attachment;
    }

    public AttachmentSaveEvent(Attachment attachment, Object source) {
        super(source);
        this.attachment = attachment;
    }

}
