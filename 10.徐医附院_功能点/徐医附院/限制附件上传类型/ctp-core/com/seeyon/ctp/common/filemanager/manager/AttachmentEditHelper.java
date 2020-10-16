package com.seeyon.ctp.common.filemanager.manager;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.common.filemanager.manager.ActionLog.ActionEnum;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.EnumUtil;
import com.seeyon.ctp.util.Strings;

public class AttachmentEditHelper {
    private static final String IS_EDITATT     = "isEditAttachment";
    private static final String ATT_SIZE       = "editAttachmentSize";
    private static final String LOG_ACTION     = "logAction";
    private static final String LOG_CREATEDATE = "logCreateDate";
    private static final String LOG_DESC       = "logDesc";
    //增加refId subRefId
    private static final String REFID          = "reference";
    private static final String SUB_REFID      = "subReference";

    /**
     * 辅助类初始化，需要从request出得到前端传过来的参数，并进行初始化
     * @param request
     */
    public AttachmentEditHelper(HttpServletRequest request) {
        String isEditAttachment = request.getParameter(IS_EDITATT);
        if (Strings.isNotBlank(isEditAttachment)) {
            this.hasEditAtt = true;
            String size = request.getParameter(ATT_SIZE);
            this.attSize = Integer.parseInt(size);
            this.reference = Long.parseLong(request.getParameter(REFID));
            this.subReference = Long.parseLong(request.getParameter(SUB_REFID));

            String[] logAction = request.getParameterValues(LOG_ACTION);
            String[] logCreateDate = request.getParameterValues(LOG_CREATEDATE);
            String[] logDesc = request.getParameterValues(LOG_DESC);
            List<ActionLog> logs = createLogs(logAction, logCreateDate, logDesc);
            this.logList = logs;
        }
    }

    private List<ActionLog> createLogs(String[] logAction, String[] logCreateDate, String[] logDesc) {
        List<ActionLog> result = new ArrayList<ActionLog>();
        int length = logAction.length;
        for (int i = 0; i < length; i++) {
            ActionLog log = new ActionLog();
            log.setAction(EnumUtil.getEnumByOrdinal(ActionEnum.class, Integer.parseInt(logAction[i])));
            if (Strings.isNotBlank(logCreateDate[i])) {
                try {
                    log.setCreateDate(Datetimes.parseDatetimeWithoutSecond(logCreateDate[i]));
                } catch (Exception e) {
                }
            }
            log.setDesc(logDesc[i]);
            result.add(log);
        }
        return result;
    }

    private Long            reference;
    private Long            subReference;
    private boolean         hasEditAtt = false;
    private int             attSize    = 0;
    private List<ActionLog> logList    = new ArrayList<ActionLog>();

    public static boolean hasEditAtt(HttpServletRequest request) {
        String isEditAttachment = request.getParameter(IS_EDITATT);
        if (Strings.isNotBlank(isEditAttachment)) {
            return true;
        }
        return false;
    }

    /**
     * 是否含有被编辑的附件
     * @return
     */
    public boolean hasEditAtt() {
        return this.hasEditAtt;
    }

    /**
     * 被编辑的附件的个数
     * @return
     */
    public int attSize() {
        return this.attSize;
    }

    /**
     * 编辑过程的操作日志
     * @return
     */
    public List<ActionLog> getLogs() {
        return this.logList;
    }

    public Long getReference() {
        return reference;
    }

    public void setReference(Long reference) {
        this.reference = reference;
    }

    public Long getSubReference() {
        return subReference;
    }

    public void setSubReference(Long subReference) {
        this.subReference = subReference;
    }
}
