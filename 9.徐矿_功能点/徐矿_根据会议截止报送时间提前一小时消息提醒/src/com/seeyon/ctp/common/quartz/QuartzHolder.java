/**
 * 
 */
package com.seeyon.ctp.common.quartz;

import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.Strings;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import java.util.*;

import static org.quartz.JobKey.jobKey;

/**
 * 步骤：<br>
 * 
 * 1. 定义任务处理器
 * <pre>
 * <code>
 *   class ABCQuartz implement QuartzJob{
 *     public void execute(Map&lt;String, String&gt; parameters){
 *         Long id = parameters.get("id");
 *         ...
 *     }
 *   }
 *   
 *   &lt;bean name=&quot;abcQuartz&quot; class=&quot;package.ABCQuartz&quot; /&gt;
 * </code>
 * </pre>
 * 2. 生成任务
 * <pre>
 * <code>
 * Map&lt;String, String&gt; parameters = new HashMap&lt;String, String&gt;();
 * parameters.put(&quot;id&quot;, String.valueOf(id));
 * QuartzHolder.newQuartzJob(&quot;jobName&quot;, new Date(109, 1, 1), &quot;abcQuartz&quot;, parameters);
 * </code>
 * </pre>
 * 
 * @author <a href="mailto:tanmf@seeyon.com">Tanmf</a>
 * @version 1.0 2008-11-25
 */
public class QuartzHolder {
	private static final Log log = CtpLogFactory.getLog(QuartzHolder.class);
	public static String instanceName;
	
	protected static final String QUARTZ_JOB_CLASS_NAME = QuartzHolder.class.getName() + ".QuartzJobClassName";
	
	/**
	 * 只运行一次，默认分组
	 * 
	 * @param jobName
	 *            任务名称，要求每一个任务唯一
	 * @param runTime
	 *            运行时间
	 * @param jobBeanId
	 *            任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 */
	public static boolean newQuartzJob(String jobName, Date runTime,
			String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		return newQuartzJob(jobName, runTime, 0, 0, jobBeanId, parameters);
	}


	/**
	 * 只运行一次
	 * 
	 * @param groupName
	 *            任务集名称，可以为<code>null</code>
	 * @param jobName
	 *            任务名称，要求每一个任务唯一
	 * @param runTime
	 *            运行时间
	 * @param jobBeanId
	 *            任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 */
	public static boolean newQuartzJob(String groupName, String jobName, Date runTime,
			String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		return newQuartzJob(groupName, jobName, runTime, 0, 0, jobBeanId, parameters);
	}
	
	/**
	 * 重复运行无限次，默认分组
	 * 
	 * @param jobName
	 *            任务名称，要求每一个任务唯一
	 * @param beginTime
	 *            任务开始时间
	 * @param repeatInterval
	 *            任务重复执行时的时间间隔
	 * @param jobBeanId
	 *            任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 */
	public static boolean newQuartzJob(String jobName, Date beginTime,
			long repeatInterval, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		return newQuartzJob(jobName, beginTime, -1, repeatInterval, jobBeanId, parameters);
	}

	/**
	 * 新开一个定时任务，默认分组
	 * 
	 * @param jobName
	 *            任务名称，要求每一个任务唯一
	 * @param beginTime
	 *            任务开始时间
	 * @param repeatCount
	 *            任务重复执行次数， -1表示无限次
	 * @param repeatInterval
	 *            任务重复执行时的时间间隔
	 * @param jobBeanId
	 *            任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean newQuartzJob(String jobName, Date beginTime,
			int repeatCount, long repeatInterval, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		return newQuartzJob(null, jobName, beginTime, repeatCount, repeatInterval, jobBeanId, parameters);
	}
	
	/**
	 * 创建每天的定时任务，时间以beginTime为准
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 */
	public static boolean newQuartzJobPerDay(String groupName, String jobName, Date beginTime, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		return newQuartzJobPerDay(groupName, jobName, beginTime,null, jobBeanId, parameters);
	}
	/**
	 * 创建每天的定时任务，时间以beginTime为准
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 */
	public static boolean newQuartzJobPerDay(String groupName, String jobName, Date beginTime,Date endTime, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		
		String cronExpression = second + " " + minute + " " + hourOfDay + " " + "*" + " * ?";
		return newCronQuartzJob(groupName, jobName,  cronExpression,beginTime,endTime,jobBeanId, parameters);		
//		return newQuartzJob(groupName, jobName, beginTime,endTime, -1, 24 * 60 * 60 * 1000, jobBeanId, parameters);
	}
	/**
	 * 创建每周的定时任务，星期几时间以beginTime那天所处的星期几为准
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime 任务执行类的BeanId, implement QuartzJob接口
	 * @param jobBeanId
	 * @param parameters
	 * @return
	 */
	public static boolean newQuartzJobPerWeek(String groupName, String jobName, Date beginTime, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		Date endTime = null;
		return newQuartzJobPerWeek(groupName, jobName, beginTime, endTime,
				jobBeanId, parameters);
	}


	public static boolean newQuartzJobPerWeek(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		int dayOfWeek = gc.get(GregorianCalendar.DAY_OF_WEEK);
		
		String cronExpression = second + " " + minute + " " + hourOfDay + " ? * " + dayOfWeek;
		return newCronQuartzJob(groupName, jobName, cronExpression, beginTime,endTime, jobBeanId, parameters);
	}
	
	/**
	 * 创建每月的定时任务，“日”以beginTime那天所处的“日”为准，如果是31日，则自动顺延
	 * 
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 */
	public static boolean newQuartzJobPerMonth(String groupName, String jobName, Date beginTime, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		Date endTime = null;
		return newQuartzJobPerMonth(groupName, jobName, beginTime, endTime,
				jobBeanId, parameters);
	}

	/**
	 * 
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime
	 * @param endTime	 
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean newQuartzJobPerMonth(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		int dayOfMonth = gc.get(GregorianCalendar.DAY_OF_MONTH);
		
		String cronExpression = second + " " + minute + " " + hourOfDay + " " + dayOfMonth + " * ?";
		return newCronQuartzJob(groupName, jobName,  cronExpression,beginTime,endTime,jobBeanId, parameters);
	}
	/**
	 * 创建每月最后一天执行的定时任务。
	 */
	public static boolean newQuartzJobEndOfMonth(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		
		String cronExpression = second + " " + minute + " " + hourOfDay + " L * ?";
		return newCronQuartzJob(groupName, jobName,  cronExpression,beginTime,endTime,jobBeanId, parameters);		
	}

	/**
	 * 创建每月倒数第N天指定时间执行的定时任务。
	 * @param groupName 分组名称
	 * @param jobName 任务名称
	 * @param beginTime 开始时间 执行的具体时间（时分秒按照该参数相应的时分秒设定）
	 * @param endTime 截止时间，任务定时执行的截止时间，若为null则没有截止时间
	 * @param days 每月倒数第几天，比如2表示每月的倒数第2天
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters 任务执行的参数
	 */
	private static boolean newQuartzJobEndOfMonth(String groupName,
												 String jobName, Date beginTime, Date endTime, int days, String jobBeanId,
												 Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		if(days < 1 || days > 25){
			log.warn("倒数第几天的参数传递不合法（取值范围：[1-25]）,当前传入的值为：" + days + "，系统自动将该参数设置为：1");
			days = 1;
		}
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);

		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);

		String cronExpression = second + " " + minute + " " + hourOfDay + " " + days + "L * ?";
		return newCronQuartzJob(groupName, jobName,  cronExpression,beginTime,endTime,jobBeanId, parameters);
	}

	/**
	 * 创建每季度的定时任务，“日”以beginTime那天所处的“日”为准，如果是31日，则自动顺延
	 * 
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 */
	public static boolean newQuartzJobPerSeason(String groupName, String jobName, Date beginTime, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		Date endTime = null;
		return newQuartzJobPerSeason(groupName, jobName, beginTime, endTime,
				jobBeanId, parameters);
	}
	/**
	 * 创建每季度执行的定时任务。
	 * @param groupName
	 * @param jobName
	 * @param beginTime
	 * @param endTime
	 * @param jobBeanId
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean newQuartzJobPerSeason(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		int dayOfMonth = gc.get(GregorianCalendar.DAY_OF_MONTH);
		int month = gc.get(GregorianCalendar.MONTH) + 1;
		int start = month % 3;
		start = start ==0 ? 3 :start;
		String cronExpression = second + " " + minute + " " + hourOfDay + " " + dayOfMonth + " " + start + "/3 ?";
		return newCronQuartzJob(groupName, jobName, cronExpression,beginTime,endTime, jobBeanId, parameters);
	}
	/**
	 * 创建每半年执行的定时任务。
	 * @param groupName
	 * @param jobName
	 * @param beginTime
	 * @param endTime
	 * @param jobBeanId
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean newQuartzJobPerHalfyear(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		int dayOfMonth = gc.get(GregorianCalendar.DAY_OF_MONTH);
		int month = gc.get(GregorianCalendar.MONTH) + 1;
		int start = month % 6;
		start = start ==0 ? 6 :start;		
		String cronExpression = second + " " + minute + " " + hourOfDay + " " + dayOfMonth + " " + start + "/6 ?";
		return newCronQuartzJob(groupName, jobName, cronExpression,beginTime,endTime, jobBeanId, parameters);
	}	
	/**
	 * 创建每半年末执行的定时任务。
	 * @param groupName
	 * @param jobName
	 * @param beginTime
	 * @param endTime
	 * @param jobBeanId
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean newQuartzJobEndOfHalfyear(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);

		String cronExpression = second + " " + minute + " " + hourOfDay + " " + "L" + " " + "6,12" + " ?";
		return newCronQuartzJob(groupName, jobName, cronExpression,beginTime,endTime, jobBeanId, parameters);
	}	
	/**
	 * 创建每季度最后一天执行的定时任务。
	 * @param groupName
	 * @param jobName
	 * @param beginTime
	 * @param endTime
	 * @param jobBeanId
	 * @param parameters
	 * @return
	 * @throws MutiQuartzJobNameException
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean newQuartzJobEndOfSeason(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);

		String cronExpression = second + " " + minute + " " + hourOfDay + " L 3,6,9,12 ?";
		return newCronQuartzJob(groupName, jobName, cronExpression,beginTime,endTime, jobBeanId, parameters);
	}	
	/**
	 * 创建每年度的定时任务，“月-日”以beginTime那天所处的“月日”为准，如果是2-29日，则自动顺延为3-1
	 * 
	 * @param groupName 可以为<code>null</code>
	 * @param jobName
	 * @param beginTime
	 * @param jobBeanId 任务执行类的BeanId, implement QuartzJob接口
	 * @param parameters
	 * @return
	 */
	public static boolean newQuartzJobPerYear(String groupName, String jobName, Date beginTime, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		Date endTime= null;
		return newQuartzJobPerYear(groupName, jobName, beginTime, endTime,
				jobBeanId, parameters);
	}


	public static boolean newQuartzJobPerYear(String groupName,
			String jobName, Date beginTime, Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(beginTime);
		
		int second = gc.get(GregorianCalendar.SECOND);
		int minute = gc.get(GregorianCalendar.MINUTE);
		int hourOfDay = gc.get(GregorianCalendar.HOUR_OF_DAY);
		int dayOfMonth = gc.get(GregorianCalendar.DAY_OF_MONTH);
		int month = gc.get(GregorianCalendar.MONTH) + 1;
		
		String cronExpression = second + " " + minute + " " + hourOfDay + " " + dayOfMonth + " " + month + " ? *";
		return newCronQuartzJob(groupName, jobName,  cronExpression,beginTime,endTime, jobBeanId, parameters);
	}
	
	public static boolean newCronQuartzJob(String groupName, String jobName,
			String cronExpression, Date beginTime,Date endTime, String jobBeanId,
			Map<String, String> parameters) throws MutiQuartzJobNameException,
            NoSuchQuartzJobBeanException {
		Scheduler sched = null;
		try {
			sched = QuartzListener.getScheduler();
		}
		catch (Exception e) {
			log.error("", e);
			return false;
		}

        TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger().withIdentity(jobName, groupName)
                .forJob(jobName, groupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression));
        if(endTime != null){
            triggerBuilder.endAt(endTime);
        }
        if(beginTime!=null) {
            Date now = new Date();
            triggerBuilder.startAt(now.after(beginTime)?now:beginTime);
        }

        Trigger trigger = triggerBuilder.build();

		return newQuartzJob(sched, jobName,groupName, trigger, jobBeanId, parameters);
	}


	public static boolean newQuartzJob(String groupName, String jobName, Date beginTime,
			int repeatCount, long repeatInterval, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		return newQuartzJob(groupName, jobName, beginTime, null,
				repeatCount, repeatInterval, jobBeanId, parameters);
	}

	public static boolean newQuartzJob(String groupName, String jobName,
			Date beginTime, Date endTime, int repeatCount, long repeatInterval,
			String jobBeanId, Map<String, String> parameters)
			throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		Scheduler sched = null;
		try {
			sched = QuartzListener.getScheduler();
		}
		catch (Exception e) {
			log.error("", e);
			return false;
		}


        SimpleTriggerImpl trigger = new SimpleTriggerImpl();
        trigger.setName(jobName);
        trigger.setGroup(groupName);
        Date now = new Date();
		if(beginTime!=null){
			trigger.setStartTime(now.after(beginTime) ? now : beginTime);
		}
		if(endTime != null) {
            trigger.setEndTime(endTime);
        }
        trigger.setRepeatCount(repeatCount);
		trigger.setRepeatInterval(repeatInterval);
		trigger.setMisfireInstruction(1);

		return newQuartzJob(sched, jobName, groupName, trigger, jobBeanId, parameters);
	}

	private static boolean newQuartzJob(Scheduler sched, String jobName, String groupName, Trigger trigger, String jobBeanId, Map<String, String> parameters) throws MutiQuartzJobNameException, NoSuchQuartzJobBeanException {
		Object quartzJob = null;
		try {
			quartzJob = AppContext.getBean(jobBeanId);
		}
		catch (Throwable e) {
			log.error("", e);
			return false;
		}
		
		if(quartzJob == null || !(quartzJob instanceof QuartzJob)){
			throw new NoSuchQuartzJobBeanException(jobBeanId);
		}

		JobDataMap datamap = new JobDataMap();
		datamap.put(QUARTZ_JOB_CLASS_NAME, jobBeanId);
		datamap.put("_JobName", jobName);
		
		if (parameters != null && !parameters.isEmpty()) {
			Set<Map.Entry<String, String>> enities = parameters.entrySet();
			for (Map.Entry<String, String> entry : enities) {
				datamap.put(entry.getKey(), entry.getValue());
			}
		}

        JobDetail job = JobBuilder.newJob(QuartzJobProxy.class).withIdentity(jobName, groupName).setJobData(datamap).build();
		
		try {
			sched.scheduleJob(job, trigger);
			return true;
		}
		catch (Exception e) {
			log.error("", e);
		}

		return false;
	}

	/**
	 * 更新Job，通过该接口可更新Job的参数以达到更改Job执行动作的目的
	 * @param groupName 分组名称
	 * @param jobName job名称
	 * @param jobBeanId BeanId
	 * @param parameters job执行参数
	 * @return 更新Job成功标识
	 * @throws NoSuchQuartzJobBeanException
	 */
	public static boolean updateQuartzJob(String groupName, String jobName, String jobBeanId, Map<String, String> parameters) throws NoSuchQuartzJobBeanException {

		Scheduler sched;
		try {
			sched = QuartzListener.getScheduler();
		}
		catch (Exception e) {
			log.error("", e);
			return false;
		}

		if(sched == null){
			log.error("Scheduler is null");
			return false;
		}

		Object quartzJob = null;
		try {
			quartzJob = AppContext.getBean(jobBeanId);
		}
		catch (Throwable e) {
			log.error("", e);
			return false;
		}

		if(quartzJob == null || !(quartzJob instanceof QuartzJob)){
			throw new NoSuchQuartzJobBeanException(jobBeanId);
		}

		JobDataMap datamap = new JobDataMap();
		datamap.put(QUARTZ_JOB_CLASS_NAME, jobBeanId);
		datamap.put("_JobName", jobName);

		if (parameters != null && !parameters.isEmpty()) {
			Set<Map.Entry<String, String>> enities = parameters.entrySet();
			for (Map.Entry<String, String> entry : enities) {
				datamap.put(entry.getKey(), entry.getValue());
			}
		}
		JobDetail job = null;
		try {
			job = sched.getJobDetail(JobKey.jobKey(jobName, groupName));
		} catch (SchedulerException e) {
			log.error("getJobDetail[" + groupName + "." + jobName + "] occur error", e);
			return false;
		}
		if(job == null){
			log.error("jobDetail[" + groupName + "." + jobName + "] not exist");
			return false;
		}


		try {
			sched.addJob(job, true, true);
		} catch (SchedulerException e) {
			log.error("addOrUpdateJob [" + groupName + "." + jobName + "] occur error", e);
			return false;
		}

		return false;
	}
	
	/**
	 * 删除任务
	 * 
	 * @param jobName 任务名称
	 * @return
	 */
	public static boolean deleteQuartzJob(String jobName){
		try {
			Scheduler sched = QuartzListener.getScheduler();
			sched.deleteJob(jobKey(jobName));
			
			return true;
		}
		catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e.getLocalizedMessage(),e);
		}
		
	}
	
	/**
	 * 删除整个分组的任务
	 * 
	 * @param groupName
	 * @return
	 */
	public static boolean deleteQuartzJobByGroup(String groupName){
		if(Strings.isBlank(groupName)){
			throw new IllegalArgumentException("groupName is null");
		}
		
		if("DEFAULT".equalsIgnoreCase(groupName)){
			throw new IllegalArgumentException("groupName is DEFAULT");
		}
		
		try {
			Scheduler sched = QuartzListener.getScheduler();
            Set<JobKey> jobKeys = sched.getJobKeys(GroupMatcher.jobGroupEquals(groupName));

            for(JobKey jobKey:jobKeys){
                sched.deleteJob(jobKey);
            }
			
			return true;
		}
		catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e.getLocalizedMessage(),e);
		}
		
	}
	
	/**
	 * 删除整个分组的任务
	 * 
	 * @param groupName
	 * @return
	 */
	public static boolean deleteQuartzJobByGroupAndJobName(String groupName,String name){
		if(Strings.isBlank(groupName)){
			throw new IllegalArgumentException("groupName is null");
		}
		
		if("DEFAULT".equalsIgnoreCase(groupName)){
			throw new IllegalArgumentException("groupName is DEFAULT");
		}
		
		try {
			Scheduler sched = QuartzListener.getScheduler();
			sched.deleteJob(jobKey(name, groupName));
			return true;
		}
		catch (Exception e) {
			log.error("", e);
			throw new RuntimeException(e.getLocalizedMessage(),e);

		}
	}
	/**
	 * 检测任务是否存在
	 * 
	 * @param jobName 任务名称
	 * @return
	 */
	public static boolean hasQuartzJob(String jobName){
		return hasQuartzJob(null, jobName);
	}

    /**
     * 检测任务是否存在
     * @param groupName 分组
     * @param jobName 任务名称
     * @return
     */
    public static boolean hasQuartzJob(String groupName, String jobName) {

        if(instanceName == null){
            loadInstanceName();
        }

    	// 因getJobDetail虽然无锁，但有事务，且存在获取Listener的语句，所以修改逻辑，直奔主题。会丢失其他JobStore的特性，但我们不使用，无影响。
		JDBCAgent agent = new JDBCAgent(true);
		try {
			String sql = "SELECT JOB_NAME FROM JK_JOB_DETAILS WHERE JOB_NAME = ? AND JOB_GROUP = ? AND SCHED_NAME = ?";
			List<String> params = new ArrayList<String>();
			params.add(jobName);
			params.add(groupName==null ? "DEFAULT" : groupName);
            params.add(instanceName);
			agent.execute(sql, params);
			return ! CollectionUtils.isEmpty(agent.resultSetToList());
		} catch (Throwable e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			agent.close();
		}

		return false;    	
    }

    private synchronized static void loadInstanceName() {
        if(instanceName == null){
            Scheduler sched = null;
            try {
                sched = QuartzListener.getScheduler();
                instanceName = sched.getSchedulerName();
            }
            catch (Exception e) {
                log.error("", e);
            }
        }
    }
}
