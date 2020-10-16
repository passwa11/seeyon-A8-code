/**
 * $Author: $
 * $Rev: $
 * $Date:: $
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.portal.section;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.bo.PendingRow;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.HANDLER_PARAMETER;
import com.seeyon.ctp.portal.section.templete.BaseSectionTemplete.OPEN_TYPE;
import com.seeyon.ctp.portal.section.templete.MultiRowVariableColumnColTemplete;
import com.seeyon.ctp.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mujun
 *
 */
public class PendingSectionUrlUtil {
      public static String getPendingDetailUrl(CtpAffair affair){
          Long affairId = affair.getId();
          Long objectId = affair.getObjectId();
          ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(affair.getApp());
          switch (appEnum) {
              case collaboration :
                  return "/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=" + affairId;
              case meetingroom:
                  return "/meetingroom.do?method=createPerm&openWin=1&id=" + objectId+"&affairId="+affairId;
                  
          }
          return "";
      }
      public static String getPendingCategoryUrl(Integer app){
          ApplicationCategoryEnum appEnum = ApplicationCategoryEnum.valueOf(app);
          switch (appEnum) {
              case collaboration :
                  return "/collaboration/collaboration.do?method=listPending&openFrom=listPending";
              case meetingroom:
                  return "/meetingroom.do?method=index";
          }
          return "";
      }
      
      public static MultiRowVariableColumnColTemplete getTemplete(MultiRowVariableColumnColTemplete c,List<PendingRow> rowList, Map<String, String> preference) {
          //显示的列
          String rowStr = preference.get("rowList");
          if (Strings.isBlank(rowStr)) {
              rowStr = "subject,receiveTime,sendUser,category";
          }
          
          for (PendingRow pendingRow : rowList) {
              MultiRowVariableColumnColTemplete.Row row = c.addRow();

              Integer app = pendingRow.getApplicationCategoryKey();
              Integer subApp = pendingRow.getApplicationSubCategoryKey();
              OPEN_TYPE openType = OPEN_TYPE.href_blank;
              if (null!=subApp && (subApp.equals(ApplicationSubCategoryEnum.old_edocRegister.getKey())
            		  ||subApp.equals(ApplicationSubCategoryEnum.old_edocRecDistribute.getKey()))) {//公文待登记与待分发
                  openType = OPEN_TYPE.href;
              }
              if (null != app && null!=subApp && app==ApplicationCategoryEnum.info.getKey() 
            		  && subApp==ApplicationSubCategoryEnum.info_magazine_publish.key()) {//公文待登记与待分发
            	  openType = OPEN_TYPE.dialog;
              }
              //标题
              MultiRowVariableColumnColTemplete.Cell subjectCell = row.addCell();
              subjectCell.setId(pendingRow.getId());
              subjectCell.setCellContentHTML(pendingRow.getSubject());
              subjectCell.setLinkURL(pendingRow.getLink(), openType);
              subjectCell.setClassName(pendingRow.getSubState() == SubStateEnum.col_pending_unRead.key() ? "ReadDifferFromNotRead" : "AlreadyReadByCurrentUser");
              subjectCell.setHasAttachments(pendingRow.getHasAttachments());
              subjectCell.setBodyType(pendingRow.getBodyType());
              subjectCell.setApp(app);
              subjectCell.setSubApp(pendingRow.getApplicationSubCategoryKey());
              subjectCell.setTop(pendingRow.getTopTime() == null ? false : true);
              //添加‘重要程度’图标
              if(pendingRow.getImportantLevel() != null && pendingRow.getImportantLevel() > 1  && pendingRow.getImportantLevel() < 6){//会议没有重要程度，非空判断
                  subjectCell.addExtPreClasses("ico16 important"+pendingRow.getImportantLevel()+"_16");
              }
              //添加视频会议图标
              if ("2".equals(pendingRow.getMeetingNature())) {
                  String videoIconClass = convertPortalBodyType("videoConf");
                  subjectCell.addExtClasses("ico16 " + videoIconClass);
              }
              //添加‘附件’图标
              if(pendingRow.getHasAttachments()){
                  subjectCell.addExtClasses("ico16 vp-attachment");
              }
              //添加‘正文类型’图标
              String bodyType = pendingRow.getBodyType();
              if(Strings.isNotBlank(bodyType) && !"10".equals(bodyType) && !"30".equals(bodyType)) {
                  String bodyTypeClass = convertPortalBodyType(bodyType);
                  bodyTypeClass = "office" + bodyTypeClass;

                  if(!"html_16".equals(bodyTypeClass)) {
                      subjectCell.addExtClasses("ico16 "+bodyTypeClass);
                  }
              }
              //是否超期图标
              if (pendingRow.getDealTimeout() && pendingRow.isShowClockIcon()) {//超期图标
                  subjectCell.addExtClasses("ico16 extended_red_16");
              } else if (pendingRow.isShowClockIcon() && !pendingRow.isOverTime()){//未超期图标
                  subjectCell.addExtClasses("ico16 extended_blue_16");
              }
              if (rowStr.indexOf("processingProgress") != -1 ) {
                  MultiRowVariableColumnColTemplete.Cell detailCell = row.addCell();
                  detailCell.setApp(app);
                  String content = "";
                  //回复数（共有{0}条回复)
                  List<Integer> replyEnums =new ArrayList<Integer>();
                  //replyEnums.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
                  //replyEnums.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
                  //replyEnums.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
                  replyEnums.add(ApplicationCategoryEnum.collaboration.getKey());//协同
                  if (replyEnums.contains(pendingRow.getApplicationCategoryKey())) {
                      Integer count = pendingRow.getReplyCounts();
                      if (null==count || count.equals(0)) {
                          content += ResourceUtil.getString("common.pending.replyCountNoData") + "&nbsp;";
                      } else {
                          content += ResourceUtil.getString("common.pending.replyCount",pendingRow.getReplyCounts()) + "&nbsp;";
                      }
                  }
                  //超期（已超期、未超期）
                  if (pendingRow.isShowClockIcon()) {
                      if (pendingRow.isOverTime() && pendingRow.getDeadlineTime()!= null && !"".equals(pendingRow.getDeadlineTime()) && !"－".equals(pendingRow.getNodeadlineTime())) {
                          content += "<span class='colorRed'>"+ResourceUtil.getString("common.pending.detailOvertime",pendingRow.getDeadlineTime()) +"</span>";
                          //detailCell.setClassName("color_red");
                      } else if (pendingRow.getShowDealLineTime() && pendingRow.getNodeadlineTime()!= null && !"".equals(pendingRow.getNodeadlineTime()) && !"－".equals(pendingRow.getNodeadlineTime())){
                          content += ResourceUtil.getString("common.pending.detailNoOvertime",pendingRow.getNodeadlineTime()) + "&nbsp;";
                      }
                  }
                  //会议：参加、不参加、待定
                  if (pendingRow.getApplicationCategoryKey() == ApplicationCategoryEnum.meeting.getKey()) {
                      content += ResourceUtil.getString("common.pending.meetingCountJoin",pendingRow.getProcessedNumber()) + "&nbsp;";
                      content += ResourceUtil.getString("common.pending.meetingCountNoJoin",pendingRow.getUnJoinNumber()) + "&nbsp;";
                      content += ResourceUtil.getString("common.pending.meetingCountPending",pendingRow.getPendingNumber()) + "&nbsp;";
                      Map<String, Map<String, String>> meetingHandler = new HashMap<String, Map<String, String>>();
                      Map<String,String> mouseoverHandler = new HashMap<String,String>();
                      String meetingCardUrl = "/meeting.do?method=showReplyCardDetail&entityId=pendingSection&meetingId="+pendingRow.getObjectId();
                      mouseoverHandler.put(HANDLER_PARAMETER.name.name(), "showMeetingCardDetail");
                      mouseoverHandler.put(HANDLER_PARAMETER.parameter.name(), meetingCardUrl);
                      meetingHandler.put(OPEN_TYPE.mouseover.name(), mouseoverHandler);
                      Map<String,String> mouseoutHandler = new HashMap<String,String>();
                      mouseoutHandler.put(HANDLER_PARAMETER.name.name(), "closeMeetingCardDetail");
                      meetingHandler.put(OPEN_TYPE.mouseout.name(), mouseoutHandler);
                      detailCell.setHandler(meetingHandler);
                      detailCell.setOpenType(OPEN_TYPE.mouseover);
                      detailCell.setId(pendingRow.getObjectId());
                      
                  }
                  //催办次数
                  Integer categoryKey = pendingRow.getApplicationCategoryKey();
                  List<Integer> hastenEnums =new ArrayList<Integer>();
                  hastenEnums.add(ApplicationCategoryEnum.edocSend.getKey());//发文 19
                  hastenEnums.add(ApplicationCategoryEnum.edocRec.getKey());//收文 20
                  hastenEnums.add(ApplicationCategoryEnum.edocSign.getKey());//签报21
                  hastenEnums.add(ApplicationCategoryEnum.exSign.getKey());//待签收公文 23
                  hastenEnums.add(ApplicationCategoryEnum.collaboration.getKey());//协同1
                  hastenEnums.add(ApplicationCategoryEnum.meeting.getKey());//会议6
                  hastenEnums.add(ApplicationCategoryEnum.meetingroom.getKey());//会议室29
                  hastenEnums.add(ApplicationCategoryEnum.inquiry.getKey());//调查10
                  Integer hastenTime = pendingRow.getHastenTimes();
                  if (hastenEnums.contains(categoryKey) && hastenTime!=null) {
                      content += ResourceUtil.getString("common.pending.RemindCount",hastenTime);
                  }
                  detailCell.setCellContentHTML(content);
                  
              }
              //接收时间
              if (rowStr.indexOf("receiveTime") != -1) {
                  MultiRowVariableColumnColTemplete.Cell receiveTimeCell = row.addCell();
                  receiveTimeCell.setCellContentHTML(pendingRow.getReceiveTime());
                  receiveTimeCell.setApp(app);
              }
              //处理期限deadLine
              if (rowStr.indexOf("deadLine") != -1) {
                  MultiRowVariableColumnColTemplete.Cell deadLineCell = row.addCell();
                  deadLineCell.setCellContentHTML(pendingRow.getDeadLine());
                  if(pendingRow.isOverTime()){
                      deadLineCell.setCellContentHTML("<span class=\"colorRed\">"+pendingRow.getDeadLine()+"</span>");
                  }
//                deadLineCell.setClassName(pendingRow.isOverTime() ? "colorRed" : "");
                  deadLineCell.setApp(app);
              }
              //公文文号edocMark
              if (rowStr.indexOf("edocMark") != -1) {
                  MultiRowVariableColumnColTemplete.Cell edocMarkCell = row.addCell();
                  String mark= pendingRow.getEdocMark();
                  edocMarkCell.setAlt(mark);
                  if(Strings.isNotBlank(mark) && mark.length() > 20){
                	  mark = mark.substring(0,20)+"...";
                  }
                  edocMarkCell.setCellContentHTML(mark);
                  edocMarkCell.setApp(app);
              }
              //发文单位sendUnit
              if (rowStr.indexOf("sendUnit") != -1) {
                  MultiRowVariableColumnColTemplete.Cell sendUnitCell = row.addCell();
                  sendUnitCell.setCellContentHTML(pendingRow.getSendUnit());
                  sendUnitCell.setApp(app);
              }
              
              //发起人sendUser
              if (rowStr.indexOf("sendUser") != -1) {
                  MultiRowVariableColumnColTemplete.Cell createMemberCell = row.addCell();
                  createMemberCell.setApp(app);
                  createMemberCell.setAlt(pendingRow.getCreateMemberName());
                  
                  String createMemberName = pendingRow.getCreateMemberName();
                  if(Strings.isNotBlank(createMemberName) && createMemberName.length() > 4){
                      createMemberName = createMemberName.substring(0, 4) + "...";
                  }
                  createMemberCell.setCellContentHTML(createMemberName);
                  
                  Map<String,Map<String,String>> sendUserHandler = new HashMap<String,Map<String,String>>();
                  Map<String,String> clickHandler = new HashMap<String, String>();
                  clickHandler.put(HANDLER_PARAMETER.name.name(), "showMemberCard");
                  clickHandler.put(HANDLER_PARAMETER.parameter.name(), pendingRow.getCreateMemberId()+"");
                  sendUserHandler.put(OPEN_TYPE.click.name(), clickHandler);
                  createMemberCell.setHandler(sendUserHandler);
                  createMemberCell.setOpenType(OPEN_TYPE.click);
                  //告知图标
                  if (pendingRow.getMeetingImpart()!=null) {
                      createMemberCell.addExtClasses("ico16 meeting_inform_16");
                      createMemberCell.addExtClassesAlt(pendingRow.getMeetingImpart());
                  }
                  //如果是会议的话-判断回执状态是否存在
                  if(Integer.valueOf(ApplicationCategoryEnum.meeting.key()).equals(pendingRow.getApplicationCategoryKey())){
                      if( !ResourceUtil.getString("mt.meeting.impart").equals(pendingRow.getMeetingImpart())){
                          //参加
                          if(SubStateEnum.meeting_pending_join.key() == pendingRow.getSubState()) {
                              subjectCell.addExtPreClasses("ico16 meeting_join_16");
//                            createMemberCell.addExtClasses("ico16 meeting_join_16");
//                            createMemberCell.addExtClassesAlt(ResourceUtil.getString("meeting.page.lable.receipt.attend"));
                          }else if(SubStateEnum.meeting_pending_pause.key() == pendingRow.getSubState()){//待定
                              subjectCell.addExtPreClasses("ico16 meeting_pause_16");
//                            createMemberCell.addExtClasses("ico16 meeting_pause_16");
//                            createMemberCell.addExtClassesAlt(ResourceUtil.getString("meeting.page.lable.receipt.notSure"));
                          }else{
                              subjectCell.addExtPreClasses("");
//                            createMemberCell.addExtClasses("");
//                            createMemberCell.addExtClassesAlt("");
                          }
                      }
                  }
                  //加签/会签图标
                  if (pendingRow.getFromName()!=null ) {
                      createMemberCell.addExtClasses("ico16 signature_16");
                      createMemberCell.addExtClassesAlt(pendingRow.getFromName());
                  }
                  //回退图标
                  if (pendingRow.getBackFromName() != null) {
                      createMemberCell.addExtClasses("ico16 specify_fallback_16");
                      createMemberCell.addExtClassesAlt(pendingRow.getBackFromName());
                  }
              }
              //上一处理人preApproverName
              if (rowStr.indexOf("preApproverName") != -1) {
                  MultiRowVariableColumnColTemplete.Cell preApproverNameCell = row.addCell();
                  preApproverNameCell.setCellContentHTML(pendingRow.getPreApproverName());
                  preApproverNameCell.setApp(app);
              }
              //会议地点placeOfMeeting
              if (rowStr.indexOf("placeOfMeeting") != -1) {
                  MultiRowVariableColumnColTemplete.Cell placeOfMeetingCell = row.addCell();
                  placeOfMeetingCell.setCellContentHTML(pendingRow.getPlaceOfMeeting());
                  placeOfMeetingCell.setApp(app);
              }
              //主持人theConferenceHost
              if (rowStr.indexOf("theConferenceHost") != -1) {
                  MultiRowVariableColumnColTemplete.Cell theConferenceHostCell = row.addCell();
                  theConferenceHostCell.setCellContentHTML(pendingRow.getTheConferenceHost());
                  Map<String, Map<String, String>> theConferenceHostHandler = new HashMap<String, Map<String, String>>();
                  Map<String,String> clickHandler = new HashMap<String,String>();
                  clickHandler.put(HANDLER_PARAMETER.name.name(), "showMemberCard");
                  clickHandler.put(HANDLER_PARAMETER.parameter.name(), pendingRow.getTheConferenceHostId()+"");
                  theConferenceHostHandler.put(OPEN_TYPE.click.name(), clickHandler);
                  theConferenceHostCell.setHandler(theConferenceHostHandler);
                  theConferenceHostCell.setOpenType(OPEN_TYPE.click);
                  theConferenceHostCell.setApp(app);
              }
              //分类
              if (rowStr.indexOf("category") != -1) {
                  MultiRowVariableColumnColTemplete.Cell categoryCell = row.addCell();
                  categoryCell.setCellContentHTML(pendingRow.getCategoryLabel());
                  Map<String, Map<String, String>> categoryHandler = new HashMap<String, Map<String, String>>();
                  Map<String,String> clickHandler = new HashMap<String,String>();
                  clickHandler.put(HANDLER_PARAMETER.name.name(), "open_link");
                  clickHandler.put(HANDLER_PARAMETER.parameter.name(), pendingRow.getCategoryLink());
                  categoryHandler.put(OPEN_TYPE.click.name(), clickHandler);
                  //如果是模板审批那么久不穿透
                  if (Integer.valueOf(ApplicationCategoryEnum.templateApprove.getKey()).equals(pendingRow.getApplicationCategoryKey())
                		  || (Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(pendingRow.getApplicationCategoryKey()) &&
                		  	   Integer.valueOf(ApplicationCategoryEnum.exSend.getKey()).equals(pendingRow.getApplicationSubCategoryKey()) )
                		  || (Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(pendingRow.getApplicationCategoryKey()) &&
                    		   Integer.valueOf(ApplicationCategoryEnum.exSign.getKey()).equals(pendingRow.getApplicationSubCategoryKey()) )
                		  || (Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(pendingRow.getApplicationCategoryKey()) &&
                    		   Integer.valueOf(ApplicationCategoryEnum.edocRegister.getKey()).equals(pendingRow.getApplicationSubCategoryKey())) 
                          || (Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(pendingRow.getApplicationCategoryKey()) &&
                               Integer.valueOf(ApplicationCategoryEnum.edocRecDistribute.getKey()).equals(pendingRow.getApplicationSubCategoryKey())) 
                	  ) {
                      categoryCell.setHandler(null);
                  }else{
                      categoryCell.setHandler(categoryHandler);
                  }
                  
                  if(Integer.valueOf(ApplicationCategoryEnum.info.getKey()).equals(pendingRow.getApplicationCategoryKey())) {
                	  if(!pendingRow.isHasResPerm()) {
                		  categoryCell.setHandler(null);  
                	  } else {
                		  categoryCell.setHandler(categoryHandler);  
                	  }
                  }

                  Integer categoryOpenType = pendingRow.getOpenType();
                  if (Integer.valueOf(ApplicationCategoryEnum.edoc.getKey()).equals(app)) {
                	  categoryOpenType = pendingRow.getCategoryOpenType();
                  }
                  if (null == categoryOpenType) {
                      categoryOpenType = OPEN_TYPE.openWorkSpace.ordinal();
                      if (AppContext.hasPlugin("workflowAdvanced") &&  Integer.valueOf(ApplicationCategoryEnum.collaboration.getKey()).equals(pendingRow.getApplicationCategoryKey())) {
                          categoryOpenType = OPEN_TYPE.multiWindow.ordinal();
                      }
                  }
                  categoryCell.setOpenType(categoryOpenType);
                  categoryCell.setApp(app);
              }
              //节点权限policy
              if (rowStr.indexOf("policy") != -1) {
                  MultiRowVariableColumnColTemplete.Cell policyCell = row.addCell();
                  policyCell.setCellContentHTML(pendingRow.getPolicyName());
                  policyCell.setApp(app);
              }
              
          }
          return c;
      }
      
      private static String convertPortalBodyType(String bodyType) {
          String bodyTypeClass = "html_16";
          if("FORM".equals(bodyType) || "20".equals(bodyType)) {
              bodyTypeClass = "form_text_16";
          } else if("TEXT".equals(bodyType) || "30".equals(bodyType)) {
              bodyTypeClass = "txt_16";
          } else if("OfficeWord".equals(bodyType) || "41".equals(bodyType)) {
              bodyTypeClass = "doc_16";
          } else if("OfficeExcel".equals(bodyType) || "42".equals(bodyType)) {
              bodyTypeClass = "xls_16";
          } else if("WpsWord".equals(bodyType) || "43".equals(bodyType)) {
              bodyTypeClass = "wps_16";
          } else if("WpsExcel".equals(bodyType) || "44".equals(bodyType)) {
              bodyTypeClass = "xls2_16";
          } else if("Pdf".equals(bodyType) || "45".equals(bodyType)) {
              bodyTypeClass = "pdf_16";
          } else if("Ofd".equals(bodyType) || "46".equals(bodyType)) {
              bodyTypeClass = "ofd_16";
          } else if("videoConf".equals(bodyType)) {
              bodyTypeClass = "meeting_video_16";
          }
          return bodyTypeClass;
      }
}
