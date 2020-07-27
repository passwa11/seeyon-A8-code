package com.seeyon.apps.collaboration.listener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seeyon.apps.edoc.event.EdocAffairsAssignedEvent;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.manager.EdocSummaryManager;


public class GongwenTijiaoListener {

    /**
     * 监听提交操作
     * 客开
     *
     * @param event
     * @author shenwei
     * 2020年4月23日
     */
    @ListenEvent(event = EdocAffairsAssignedEvent.class, async = true)
    public void doLog(EdocAffairsAssignedEvent event) {
        System.out.println("进来了");

        List<CtpAffair> list = event.getAffairs();
        if (list.size() > 0) {
            String nowquanxian = list.get(0).getNodePolicy();
            String pquanxian = "";
            Date date=list.get(0).getUpdateDate();
            if (nowquanxian.equals("批示")|| nowquanxian.equals("办理") ) {
                pquanxian = "转送";
            }

            if (pquanxian.equals("转送")) {
                //父节点是固定的竞争执行节点时走新的竞争执行流程
                AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
                EdocSummaryManager edocSummaryManager=(EdocSummaryManager)AppContext.getBean("edocSummaryManager");
                List<CtpAffair> plist = new ArrayList<CtpAffair>();
                try {
                    plist = affairManager.getAffairsByNodePolicy(pquanxian);
                } catch (BusinessException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (plist.size() > 0) {
                    for (CtpAffair ctpAffair : plist) {
                        if (list.get(0).getObjectId().longValue() == ctpAffair.getObjectId().longValue()) {
                            ctpAffair.setState(4);
                            ctpAffair.setSubState(0);
                            ctpAffair.setUpdateDate(date);
//                            EdocSummary edoc=edocSummaryManager.findById(ctpAffair.getObjectId().longValue());
//                            if(null !=edoc){
//                                EdocSummary edocSummary=new EdocSummary();
//                                edocSummary.setId(edoc.getId());
//                                Timestamp timestamp=new Timestamp(System.currentTimeMillis());
//                                edocSummary.setUpdateTime(timestamp);
//                                edocSummary.setCreateTime(edoc.getCreateTime());
//                                edocSummary.setEdocType(edoc.getEdocType());
//                                edocSummary.setCanTrack(edoc.getCanTrack());
//                                edocSummary.setState(edoc.getState());
//                                edocSummary.setSubject(edoc.getSubject());
//                                edocSummaryManager.updateEdocSummary(edocSummary);
//                            }

                            try {
                                affairManager.updateAffair(ctpAffair);
                            } catch (BusinessException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
//		MobileMessageManager mobileMessageManager = (MobileMessageManager) AppContext.getBean("mobileMessageManager");
//		OrgManager orgManager =(OrgManager) AppContext.getBean("orgManager");
//
//		for (CtpAffair ctpAffair : list) {
//			if(ctpAffair.getTempleteId().longValue()==-8179706758891550586L||ctpAffair.getTempleteId().longValue()==-2753664714455955870L||ctpAffair.getTempleteId().longValue()==-5239532817707808301L||ctpAffair.getTempleteId().longValue()==3870101960471038245L)
//			{
//				try
//				{
//
//						List<Long> legitimacyReceiverIdsList = new ArrayList<Long>();
//						V3xOrgMember member = orgManager.getMemberById(ctpAffair.getMemberId());
//						if (member != null) {
//		                       if (Strings.isNotBlank(member.getTelNumber())) {
//		                           legitimacyReceiverIdsList.add(ctpAffair.getMemberId());
//		                       }
//		                }
//						if(legitimacyReceiverIdsList.size()>0)
//						{
//							Long[] receiverIdsArray = (Long[]) legitimacyReceiverIdsList.toArray(new Long[legitimacyReceiverIdsList
//							                                                                              .size()]);
//							String content="您有一条公文待办，《"+ctpAffair.getSubject()+"》，请尽快登录OA处理。";
//							//mobileMessageManager.sendMobilePersonMessage(content, user.getId(), new Date(),
//				            //        receiverIdsArray);
//							String params = createJson(member.getTelNumber(),ctpAffair.getSubject());
//							System.out.println("params="+params);
//							String result = HttpService.doPost("http://32.114.72.6:8001/ESBService/JSON/JsonProxyService", params);
//							System.out.println("result="+result);
//
//						}
//				}
//				catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}


    }


}
