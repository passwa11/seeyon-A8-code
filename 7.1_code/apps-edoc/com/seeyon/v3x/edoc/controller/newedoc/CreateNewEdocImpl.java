package com.seeyon.v3x.edoc.controller.newedoc;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;

import com.seeyon.apps.meeting.api.MeetingApi;
import com.seeyon.apps.meeting.bo.MtSummaryBO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.Constants;
import com.seeyon.ctp.common.po.filemanager.Attachment;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocBody;
import com.seeyon.v3x.edoc.domain.EdocSummary;

public class CreateNewEdocImpl extends NewEdocHandle {

    private static final Log LOG = LogFactory.getLog(CreateNewEdocImpl.class);

	@Override
    public void createEdocSummary(HttpServletRequest request, ModelAndView modelAndView) {
        ///////////////////////////////初始化公文对象
        summary = new EdocSummary();
        body = new EdocBody();

        // lijl通过edoc_register的Id获取地象在发文单中显示----------------------------------开始
        String idStr = request.getParameter("id");
        if (idStr != null && !"".equals(idStr)) {
            /* puyc 分发 收文的summaryId */
            modelAndView.addObject("recSummaryId", idStr);
        }
        // lijl通过edoc_register的Id获取地象在发文单中显示----------------------------------结束

        ///////////////////////////////公文属性值设置
        summary.setOrgAccountId(user.getLoginAccount());
        summary.setEdocType(iEdocType);
        summary.setCanTrack(1);
        summary.setCreatePerson(user.getName());

        ///////////////////////////////公文正文类型，如果没有office控件，默认显示为html
        //body.setContentType(EdocHelper.getEdocBodyContentType(bodyContentType));

        if (null == bodyContentType) {
            bodyContentType = com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_OFFICE_WORD;
            if (com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx") == false) {
                bodyContentType = com.seeyon.ctp.common.constants.Constants.EDITOR_TYPE_HTML;
            }
        }

        summary.getEdocBodies().add(body);

        //会议纪要转发公文 --yangfan
        if (Strings.isNotBlank(meetingSummaryId)) {
            MtSummaryBO mtSummary = null;
            
            if(AppContext.hasPlugin("meeting")){
                try {
                	MeetingApi meetingApi = (MeetingApi) AppContext.getBean("meetingApi");
                    mtSummary = meetingApi.getMtSummary(Long.parseLong(meetingSummaryId));
                } catch (Exception e) {
                    LOG.error("获取会议纪要报错", e);
                }
            }
            
            if (mtSummary != null) {
                summary.setSubject(mtSummary.getMtName());//set纪要标题到 公文标题
                //转发正文
                EdocBody body2 = new EdocBody();
                String bodyContentType2 = mtSummary.getBodyType();
                if (com.seeyon.ctp.common.SystemEnvironment.hasPlugin("officeOcx") == false) {
                    bodyContentType2 = Constants.EDITOR_TYPE_HTML;
                }
                body2.setContentType(bodyContentType2);
                body2.setContent(mtSummary.getContent());
                summary.getEdocBodies().clear();
                summary.getEdocBodies().add(body2);

                List<Attachment> nowAtt = attachmentManager.getByReference(Long.parseLong(meetingSummaryId),
                        Long.parseLong(meetingSummaryId));
                List<Attachment> exclude2List = new ArrayList<Attachment>();//需要重新new一个List，不能在atts的基础上使用remove.
                for (Attachment att : nowAtt) {
                    if (!Integer.valueOf(2).equals(att.getType()))
                        exclude2List.add(att);
                }
                atts = exclude2List;
            }
        }
        //会议纪要转发公文 --yangfan

        ///////////////////////////////设置默认公文单，subType在方法getNewDefaultEdocForm
        defaultEdocForm = edocManager.getNewDefaultEdocForm(iEdocType, subType, user);
        modelAndView.addObject("subType", subType);
        comm = "new_form";//代表第一次保存
    }

}
