package com.seeyon.apps.collaboration.listener;

import java.util.*;

import com.seeyon.apps.edoc.event.EdocAffairsAssignedEvent;
import com.seeyon.apps.ext.temp.manager.XkjtTempManager;
import com.seeyon.apps.ext.temp.manager.XkjtTempManagerImpl;
import com.seeyon.apps.ext.temp.po.XkjtTemp;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;
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
    public void doLog(EdocAffairsAssignedEvent event) throws BusinessException {
        List<CtpAffair> list = event.getAffairs();
        if (list.size() > 0) {
            String nowquanxian = list.get(0).getNodePolicy();
            String pquanxian = "";
            Date date = list.get(0).getUpdateDate();
            if (nowquanxian.equals("批示") || nowquanxian.equals("办理")) {
                pquanxian = "转送";
            }

            if (pquanxian.equals("转送")) {
                //父节点是固定的竞争执行节点时走新的竞争执行流程
                AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
                EdocSummaryManager edocSummaryManager = (EdocSummaryManager) AppContext.getBean("edocSummaryManager");
                List<CtpAffair> plist = new ArrayList<CtpAffair>();
                try {
                    plist = affairManager.getAffairsByNodePolicy(pquanxian, list.get(0).getObjectId().longValue());
                } catch (BusinessException e1) {
                    e1.printStackTrace();
                }
                if (plist.size() > 0) {
                    String hql = "update CtpAffair a set a.state=:state ,a.subState=:subState where id=:id";
                    Map<String, Object> phql = null;
                    XkjtTempManager tempManager = new XkjtTempManagerImpl();
                    Map<String, Object> tp = new HashMap<>();
                    tp.put("summaryId", Long.toString(list.get(0).getObjectId().longValue()));
                    List<XkjtTemp> temps = tempManager.findXkjtTemp(tp);
                    List<String> stringList = new ArrayList<>();
                    if (temps.size() > 0) {
                        for (XkjtTemp t : temps) {
                            stringList.add(t.getId());
                        }
                    }
                    for (CtpAffair ctpAffair : plist) {
                        phql = new HashMap<>();
                        if (list.get(0).getObjectId().longValue() == ctpAffair.getObjectId().longValue()) {
                            if (stringList.size() > 0) {
                                if (!stringList.contains(Long.toString(ctpAffair.getId()))) {
                                    phql.put("state", 4);
                                    phql.put("subState", 0);
                                    phql.put("id", ctpAffair.getId());
                                    affairManager.update(hql, phql);
                                }
                            } else {
                                phql.put("state", 4);
                                phql.put("subState", 0);
                                phql.put("id", ctpAffair.getId());
                                affairManager.update(hql, phql);
                            }
                        }
                    }
                    tempManager.deleteXkjtTemp(temps);
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
