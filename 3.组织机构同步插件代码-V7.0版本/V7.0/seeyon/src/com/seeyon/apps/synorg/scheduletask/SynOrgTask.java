package com.seeyon.apps.synorg.scheduletask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import com.seeyon.apps.synorg.constants.SynOrgConstants.SYNC_TIME_TYPE;
import com.seeyon.apps.synorg.constants.SynOrgConstants.SynOrgTriggerDate;
import com.seeyon.apps.synorg.manager.SyncOrgManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.quartz.QuartzHolder;
import com.seeyon.ctp.common.quartz.QuartzJob;
import com.seeyon.ctp.common.quartz.QuartzListener;

/**
 * @author Yang.Yinghai
 * @date 2016年8月2日下午4:52:12
 * @Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class SynOrgTask implements QuartzJob {

    /** 日志管理器 */
    private final static Log logger = LogFactory.getLog(SynOrgTask.class);

    /** 指定同步日期（默认为每天） */
    private static SynOrgTriggerDate triggerDate = SynOrgTriggerDate.everyday;

    /**
     * 指定同步的小时和分钟数（默认为:23:30）
     */
    private static int triggerHour = 23;

    private static int triggerMinute = 30;

    /**
     * 间隔同步的小时和分钟数（默认为:10分钟）
     */
    private static int intervalHour = 0;

    private static int intervalMin = 10;

    /** 同步类型默认为：轮循同步 */
    private static int synchTimeType = SYNC_TIME_TYPE.intervalTime.ordinal();

    /** 组织同步管理器 */
    private SyncOrgManager syncOrgManager;

    /** 同步范围：Department、Member、Post、Level */
    private static String synScope;

    /** 是否同步人员密码 */
    private static boolean isSynPassword = false;

    /** 人员岗位信息为空时默认岗位名 */
    private static String defaultPostName = "其它";

    /** 人员职务信息为空时默认职务名称 */
    private static String defaultLevelName = "其它";

    /** 创建人员时人员的默认密码 */
    private static String defaultPassword = "seeyon123456*#";

    /** 中间库根部门编码 */
    private static String rootDeptCode;
    
    /** 定义的任务插件ID */
    private static String jobBeanId="syncOrgTask";
    
    /** 定义的任务名称 */
    private static String jobName="QuartzSynDocTask";

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Map<String, String> parameters) {
        if(syncOrgManager == null) {
            syncOrgManager = (SyncOrgManager)AppContext.getBean("syncOrgManager");
        }
        if(syncOrgManager != null) {
            logger.info("组织模型自动同步开始");
            if(syncOrgManager.isSyning()) {
                logger.warn("上次同步未结束,本次同步终止");
                return;
            }
            // 设置启动标记
            syncOrgManager.setSyning(true);
            try {
                syncOrgManager.syncThirdOrgDataToSeeyon();
            } catch(Exception e1) {
                logger.error("error", e1);
            }
            // 恢复启动标记
            syncOrgManager.setSyning(false);
            logger.info("组织模型自动同步结束");
        }
    }
    
	  /**
     * 注册同步任务（V7.0）
     * @param canExcute 是否启用同步
     */
    public static void registerSyncTask(boolean canExcute) {
    	logger.info("注册组织机构同步调度任务" + SynOrgTask.class.getSimpleName() + "...");
        try {
            if(!canExcute) {
                System.out.println("文档归档自动同步未启用！");
                logger.info("文档归档自动同步未启用！");
                QuartzHolder.deleteQuartzJob(jobName);
                return;
            }
        	//先删除任务
        	QuartzHolder.deleteQuartzJob(jobName);
            // 设定时间
            if(synchTimeType == SYNC_TIME_TYPE.setTime.ordinal()) {
                if(SynOrgTriggerDate.everyday.equals(triggerDate)) {
                	Map<String, String> parameters = new HashMap<String, String>();
        			//设置时间
        			Calendar calendar=Calendar.getInstance();
        		    calendar.setTime(new Date());  //年月日  也可以具体到时分秒如calendar.set(2015, 10, 12,11,32,52); 
        		                                   //每日运行，只要改动 时间即可
        		    calendar.set(Calendar.HOUR_OF_DAY, triggerHour);
        		    calendar.set(Calendar.MINUTE, triggerMinute);
        		    calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 5);
        		    Date date=calendar.getTime();//date就是你需要的时间
        			QuartzHolder.newQuartzJobPerDay(null, jobName, date,jobBeanId, parameters);
                } else {
                	Map<String, String> parameters = new HashMap<String, String>();
        			//设置时间
        			Calendar calendar=Calendar.getInstance();
        		    calendar.setTime(new Date());  //年月日  也可以具体到时分秒如calendar.set(2015, 10, 12,11,32,52); 
        		                                   //每日运行，只要改动 时间即可
        		    calendar.set(Calendar.HOUR_OF_DAY, triggerHour);
        		    calendar.set(Calendar.MINUTE, triggerMinute);
        		    calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 5);
        		    calendar.set(Calendar.DAY_OF_WEEK, triggerDate.key());//星期// 日~六 ： 1~7（星期日是1 ，星期二是3）：获取：calendar.get(Calendar.DAY_OF_WEEK)
        		    Date date=calendar.getTime();//date就是你需要的时间
        			QuartzHolder.newQuartzJobPerWeek(null, jobName, date, jobBeanId, parameters);
                }

            }
            // 间隔时间
            else {
                int delta = (intervalHour * 60 + intervalMin) * 60 * 1000;
            	Map<String, String> parameters = new HashMap<String, String>();
                QuartzHolder.newQuartzJob(jobName, new Date(),delta,jobBeanId, parameters);
            }
            
            logger.info("注册组织机构同步调度任务" + SynOrgTask.class.getSimpleName() + ",成功");
            return;
        } catch(Exception e) {
        	logger.error("注册组织机构同步调度任务失败", e);
        }
    }

    /**
     * 获取触发日期
     * @return 触发日期
     */
    public static SynOrgTriggerDate getTriggerDate() {
        return triggerDate;
    }

    /**
     * 设置触发日期
     * @param date 触发日期
     * @return 触发日期
     */
    public static void setTriggerDate(SynOrgTriggerDate date) {
        triggerDate = date;
    }

    /**
     * 设置触发时间
     * @param hour 小时
     * @param minute 分
     */
    public static void setTriggerTime(int hour, int minute) {
        triggerHour = hour;
        triggerMinute = minute;
    }

    /**
     * 获取触发时间小时
     * @return 触发时间小时
     */
    public static int getTriggerHour() {
        return triggerHour;
    }

    /**
     * 获取触发分钟
     * @return 触发时间分钟
     */
    public static int getTriggerMinute() {
        return triggerMinute;
    }

    /**
     * 设置轮循同步时间
     * @param intervalHour 间隔小时
     * @param intervalMin 间隔分钟
     * @return
     */
    public static void setIntervalTime(int intervalHour, int intervalMin) {
        SynOrgTask.intervalHour = intervalHour;
        SynOrgTask.intervalMin = intervalMin;
    }

    /**
     * 获取 轮循同步时间小时
     * @return 轮循同步时间小时
     */
    public static int getIntervalHour() {
        return intervalHour;
    }

    /**
     * 获取 轮循同步时间小时
     * @return 轮循同步时间小时
     */
    public static int getIntervalMin() {
        return intervalMin;
    }

    /**
     * 设置 synchTimeType
     * @return synchTimeType
     */
    public static int getSynchTimeType() {
        return synchTimeType;
    }

    /**
     * 设置 synchTimeType
     * @param synchTimeType
     */
    public static void setSynchTimeType(int synchTimeType) {
        SynOrgTask.synchTimeType = synchTimeType;
    }

    /**
     * 设置 syncOrgManager
     * @param syncOrgManager
     */
    public void setSyncOrgManager(SyncOrgManager syncOrgManager) {
        this.syncOrgManager = syncOrgManager;
    }

    /**
     * 获取synScope
     * @return synScope
     */
    public static String getSynScope() {
        return synScope;
    }

    /**
     * 设置synScope
     * @param synScope synScope
     */
    public static void setSynScope(String synScope) {
        SynOrgTask.synScope = synScope;
    }

    /**
     * 获取isSynPassword
     * @return isSynPassword
     */
    public static boolean isSynPassword() {
        return isSynPassword;
    }

    /**
     * 设置isSynPassword
     * @param isSynPassword isSynPassword
     */
    public static void setSynPassword(boolean isSynPassword) {
        SynOrgTask.isSynPassword = isSynPassword;
    }

    /**
     * 获取defaultPostName
     * @return defaultPostName
     */
    public static String getDefaultPostName() {
        return defaultPostName;
    }

    /**
     * 设置defaultPostName
     * @param defaultPostName defaultPostName
     */
    public static void setDefaultPostName(String defaultPostName) {
        SynOrgTask.defaultPostName = defaultPostName;
    }

    /**
     * 获取defaultLevelName
     * @return defaultLevelName
     */
    public static String getDefaultLevelName() {
        return defaultLevelName;
    }

    /**
     * 设置defaultLevelName
     * @param defaultLevelName defaultLevelName
     */
    public static void setDefaultLevelName(String defaultLevelName) {
        SynOrgTask.defaultLevelName = defaultLevelName;
    }

    /**
     * 获取defaultPassword
     * @return defaultPassword
     */
    public static String getDefaultPassword() {
        return defaultPassword;
    }

    /**
     * 设置defaultPassword
     * @param defaultPassword defaultPassword
     */
    public static void setDefaultPassword(String defaultPassword) {
        SynOrgTask.defaultPassword = defaultPassword;
    }

    /**
     * 获取rootDeptCode
     * @return rootDeptCode
     */
    public static String getRootDeptCode() {
        return rootDeptCode;
    }

    /**
     * 设置rootDeptCode
     * @param rootDeptCode rootDeptCode
     */
    public static void setRootDeptCode(String rootDeptCode) {
        SynOrgTask.rootDeptCode = rootDeptCode;
    }
}
