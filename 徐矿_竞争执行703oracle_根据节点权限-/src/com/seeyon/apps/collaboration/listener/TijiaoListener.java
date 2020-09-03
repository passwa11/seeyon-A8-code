package com.seeyon.apps.collaboration.listener;

import java.util.*;

import com.seeyon.apps.collaboration.event.CollaborationAffairsAssignedEvent;
import com.seeyon.apps.ext.temp.manager.XkjtTempManager;
import com.seeyon.apps.ext.temp.manager.XkjtTempManagerImpl;
import com.seeyon.apps.ext.temp.po.XkjtTemp;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.content.affair.AffairManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.util.annotation.ListenEvent;


public class TijiaoListener {

    /**
     * 监听提交操作
     * 客开
     *
     * @param event
     * @author shenwei
     * 2020年4月23日
     */
    @ListenEvent(event = CollaborationAffairsAssignedEvent.class, async = true)
    public void doLog(CollaborationAffairsAssignedEvent event) throws BusinessException {

        System.out.println("进来了");

        List<CtpAffair> list = event.getAffairs();
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                String nowquanxian = list.get(i).getNodePolicy();
                String pquanxian = "";
                Date updateTime=list.get(i).getUpdateDate();
                if (nowquanxian.equals("请假集团领导")) {
                    pquanxian = "请假转送";
                }
                if (pquanxian.equals("请假转送")) {
                    //父节点是固定的竞争执行节点时走新的竞争执行流程
                    AffairManager affairManager = (AffairManager) AppContext.getBean("affairManager");
                    List<CtpAffair> plist = new ArrayList<CtpAffair>();
                    try {
                        plist = affairManager.getAffairsByNodePolicy(pquanxian,list.get(0).getObjectId().longValue());
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
                        List<String> backList = new ArrayList<>();//取回的数据的集合
                        if (temps.size() > 0) {
                            for (XkjtTemp t : temps) {
                                if (null != t.getFlag() && !"".equals(t.getFlag())) {
                                    backList.add(t.getId());
                                } else {
                                    stringList.add(t.getId());
                                }
                            }
                        }
                        if (backList.size() > 0) {
                            for (int j = 0; i < backList.size(); j++) {
                                phql = new HashMap<>();
                                phql.put("state", 4);
                                phql.put("subState", 0);
                                phql.put("id", Long.parseLong(backList.get(j)));
                                affairManager.update(hql, phql);
                            }
                        } else {
                            for (CtpAffair ctpAffair : plist) {
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
