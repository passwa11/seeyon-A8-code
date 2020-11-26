/**
 * 
 */
package com.seeyon.apps.collaboration.quartz;

import com.seeyon.apps.collaboration.bo.BackgroundDealParamBO;
import com.seeyon.apps.collaboration.bo.BackgroundDealResult;
import com.seeyon.apps.collaboration.enums.BackgroundDealType;

/** 
* @Description: 服务器线程处理协同的接口，例如 定时任务、重复跳过、智能处理等
* @author muj
* @date 2018年3月7日 上午11:13:53 
*  
*/
public interface CollProcessBackgroundManager {
    
    public BackgroundDealResult transFinishWorkItem(BackgroundDealParamBO threadDealParamBO) throws Exception;
    
    public void transFinishWorkItem(Long affairId,BackgroundDealType dealType) throws Exception;
}
