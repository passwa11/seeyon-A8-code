package com.seeyon.apps.customFieldCtrl.kit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期处理工具类
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public class DateKit {

    private static SimpleDateFormat simple = new SimpleDateFormat("yyyyMMdd");
    
    private static SimpleDateFormat sixFormat = new SimpleDateFormat("yyMMdd");

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private static SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

    private static SimpleDateFormat weekDay = new SimpleDateFormat("MM-dd");

    private static SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
    
    private static SimpleDateFormat detailDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    
    private static final String dayNames[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五","星期六" };  
    
    public static int getYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
    
    public static String getSixDate() {
    	return getSixDate(null);
    }
    
    public static String getSixDate(Date date) {
    	if(null == date) {
    		return sixFormat.format(new Date());
    	}
    	return sixFormat.format(date);
    }
    
    /**
     * 获取这个时间属于一年的第几周
     * @param date
     * @return
     */
    public static int getWeekNum(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week =  cal.get(Calendar.WEEK_OF_YEAR);
        // 如果是星期天，那么需要减1
        if("星期日".equals(DateKit.getDayWeekName(date))) {
        	week -= 1;
        }
        return week;
    }
    
    /**
     * Description:
     * 
     * <pre>
     * 获取一周
     * </pre>
     * 
     * @param date
     * @return
     */
    public static void getWeekInterval(Map<String, Object> param) {
        Calendar cal = Calendar.getInstance();
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if(1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        param.put("startDate", cal.getTime());
        // System.out.println("所在周星期一的日期：" + imptimeBegin);
        cal.add(Calendar.DATE, 6);
        param.put("endDate", cal.getTime());
        // System.out.println("所在周星期日的日期：" + imptimeEnd);
    }

    /**
     * Description:
     * 
     * <pre>
     * 根据传入的日期，获取当前日期所在的一周时间段
     * </pre>
     * 
     * @param date
     * @return
     */
    public static void getWeekInterval(Map<String, Object> param, Date now) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        // 判断要计算的日期是否是周日，如果是则减一天计算周六的，否则会出问题，计算到下一周去了
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);// 获得当前日期是一个星期的第几天
        if(1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // System.out.println("要计算日期为:" + sdf.format(cal.getTime())); // 输出要计算日期
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        String time = DateKit.getDayDate(cal.getTime()) + " 00:00:00";
        try {
            param.put("beginDate", DateKit.getdayDate(time));
        } catch(Exception e) {
            // TODO
        }
        // System.out.println("所在周星期一的日期：" + imptimeBegin);
        cal.add(Calendar.DATE, 6);
        time = DateKit.getDayDate(cal.getTime()) + " 23:59:59";
        try {
            param.put("endDate", DateKit.getdayDate(time));
        } catch(Exception e) {
            //
        }
        // System.out.println("所在周星期日的日期：" + imptimeEnd);
    }

    public static String getSimpleDate(Date date) {
        if(date == null) {
            return "";
        }
        return simple.format(date);
    }
    
    /**
     * 给日志格式化使用
     * @param date
     * @return
     */
    public static String getDate4Cal(Date date) {
        if(date == null) {
            return "";
        }
        return hour.format(date);
    }

    public static String getDateString(Date date) {
        if(date == null) {
            return "";
        }
        return sdf.format(date);
    }

    public static String getDayDate(Date date) {
        if(date == null) {
            return "";
        }
        return dayFormat.format(date);
    }

    public static String getWeekDay(Date date) {
        if(date == null) {
            return "";
        }
        return weekDay.format(date);
    }

    /**
     * Description:
     * 
     * <pre>
     * 获取多少天以后的日期
     * </pre>
     * 
     * @param day
     * @return
     */
    public static Date getDayAfter(int day) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }
    // 获得某天的几天后
    public static Date getDayAfter(Date d, int day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, day);
        return cal.getTime();
    }
    
    /**
     * 获取一个月有多少天
     * @param year
     * @param month
     * @return
     */
    public static int getDaysByYearMonth(int year, int month) {
        Calendar a = Calendar.getInstance();
        a.set(Calendar.YEAR, year);
        a.set(Calendar.MONTH, month);
        a.set(Calendar.DATE, 1);
        a.roll(Calendar.DATE, -1);
        int maxDate = a.get(Calendar.DATE);
        return maxDate;
    }
    
    /**
     * 获取今天是星期几
     * @param date
     * @return
     */
    public static String getDayWeekName(Date date) {
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;  
        if(dayOfWeek < 0)
            dayOfWeek = 0;  
        return dayNames[dayOfWeek];  
    }

    /**
     * 解析时间，先解析yyyy-MM-dd HH:mm:ss
     * @param dateStr
     * @return
     * @throws ParseException 
     * @throws Exception
     */
    public static Date getdayDate(String dateStr) throws Exception {
        try {
            return sdf.parse(dateStr);
        } catch(Exception e) {
        	try {
        		return detailDate.parse(dateStr);
        	} catch(Exception e1) {
        		try {
        			return dayFormat.parse(dateStr);
        		} catch(Exception e2) {
        			try {
        				return simple.parse(dateStr);
        			}
        			catch(Exception e3) {
        				return monthFormat.parse(dateStr);
        			}
        		} 
        	}
        }
    }
    
    /**
     * 根据时间获取周区间字符串
     * @param now
     * @return 第XX周：2018-08-27至2018-09-02
     */
    public static String getWeekNumAndDateZone(Date now) {
    	StringBuffer sb = new StringBuffer();
    	sb.append("第");
    	sb.append(getWeekNum(now));
    	sb.append("周:");
    	Map<String, Object> param = new HashMap<String, Object>();
    	getWeekInterval(param, now);
    	Date beginDate = (Date)param.get("beginDate");
    	Date endDate = (Date)param.get("endDate");
    	sb.append(dayFormat.format(beginDate));
    	sb.append("至");
    	sb.append(dayFormat.format(endDate));
    	return sb.toString();
    }
    
    /**
     * 根据日期和时间获取一个返回该日期该时间的date
     * @param d 日期
     * @param time 时间 HH:mm格式
     * @return d日期+time时间
     * @throws ParseException 
     */
    public static Date getDateFromTime(Date d, String time) throws ParseException {
    	String dateTime = dayFormat.format(d) + " " + time;
    	return detailDate.parse(dateTime);
    }
}
