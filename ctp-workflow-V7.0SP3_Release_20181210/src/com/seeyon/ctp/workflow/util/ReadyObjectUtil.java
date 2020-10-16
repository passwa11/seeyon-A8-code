/**
 * $Author: wangchw $
 * $Rev: 23245 $
 * $Date:: 2013-05-20 19:34:02#$:
 *
 * Copyright (C) 2012 Seeyon, Inc. All rights reserved.
 *
 * This software is the proprietary information of Seeyon, Inc.
 * Use is subject to license terms.
 */
package com.seeyon.ctp.workflow.util;

import java.util.ArrayList;
import java.util.List;

import com.seeyon.ctp.workflow.vo.ReadyObjectVO;

import net.joinwork.bpm.definition.BPMProcess;
import net.joinwork.bpm.definition.BPMActivity;
import net.joinwork.bpm.definition.ReadyObject;

import com.seeyon.ctp.util.json.JSONUtil;
/**
 * <p>Title: T4 JSON工具类</p>
 * <p>Description: 工作流-ReadyObject与ReadyObjectVO对象相互转换的工具类</p>
 * <p>Copyright: Copyright (c) 2012</p>
 * <p>Company: seeyon.com</p>
 * @since CTP2.0
 */
public class ReadyObjectUtil {
    
    public static ReadyObject parseToReadyObject(String jsonString, BPMProcess process){
        ReadyObjectVO vo = JSONUtil.parseJSONString(jsonString, ReadyObjectVO.class);
        ReadyObject obj = null;
        if(vo!=null && 
                (
                        (vo.getActivityIdList()!=null && vo.getActivityIdList().size()>0) 
                        || (vo.getPreDelActivityIdList()!=null || vo.getPreDelActivityIdList().size()>0)
                )){
            obj = voToReadyObject(vo, process);
        }
        return obj;
    }
    
    public static String readyObjectToJSON(ReadyObject obj){
        ReadyObjectVO vo = readyObjectToVO(obj);
        String jsonString = "";
        if(vo!=null && 
                (
                        (vo.getActivityIdList()!=null && vo.getActivityIdList().size()>0) 
                        || (vo.getPreDelActivityIdList()!=null && vo.getPreDelActivityIdList().size()>0)
                )){ 
            jsonString = JSONUtil.toJSONString(vo);
        }
        return jsonString;
    }
    
    private static ReadyObject voToReadyObject(ReadyObjectVO vo, BPMProcess process){
        ReadyObject obj = null;
        if(vo!=null){
            obj = new ReadyObject();
            obj.setCaseId(vo.getCaseId());
            obj.setProcessId(vo.getProcessId());
            obj.setSaveTheCaseFlag(vo.getSaveTheCaseFlag());
            obj.setUserId(vo.getUserId());
            if(process!=null){
                if(vo.getActivityIdList()!=null && vo.getActivityIdList().size()>0){
                    List<BPMActivity> activityList = new ArrayList<BPMActivity>();
                    for(String isString : vo.getActivityIdList()){
                        BPMActivity node = process.getActivityById(isString);
                        if(node!=null){
                            activityList.add(node);
                        }
                    }
                    obj.setActivityList(activityList);
                }
                if(vo.getPreDelActivityIdList()!=null && vo.getPreDelActivityIdList().size()>0){
                    List<BPMActivity> preDelActivityList = new ArrayList<BPMActivity>();
                    for(String isString : vo.getPreDelActivityIdList()){
                        BPMActivity node = process.getActivityById(isString);
                        if(node!=null){
                            preDelActivityList.add(node);
                        }
                    }
                    obj.setPreDelActivityList(preDelActivityList);
                }
            }
        }
        return obj;
    }
    
    private static ReadyObjectVO readyObjectToVO(ReadyObject obj){
        ReadyObjectVO vo = null;
        if(obj!=null){
            vo = new ReadyObjectVO();
            vo.setCaseId(obj.getCaseId());
            vo.setProcessId(obj.getProcessId());
            vo.setSaveTheCaseFlag(obj.isSaveTheCaseFlag());
            vo.setUserId(obj.getUserId());
            if(obj.getActivityList()!=null && obj.getActivityList().size()>0){
                List<String> activityIdList = new ArrayList<String>(obj.getActivityList().size());
                for(BPMActivity node : obj.getActivityList()){
                    activityIdList.add(node.getId());
                }
                vo.setActivityIdList(activityIdList);
            }
            if(obj.getPreDelActivityList()!=null && obj.getPreDelActivityList().size()>0){
                List<String> preDelActivityIdList = new ArrayList<String>();
                for(BPMActivity node : obj.getPreDelActivityList()){
                    preDelActivityIdList.add(node.getId());
                }
                vo.setPreDelActivityIdList(preDelActivityIdList);
            }
        }
        return vo;
    }
    
}
