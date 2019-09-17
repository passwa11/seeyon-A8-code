package com.seeyon.v3x.edoc.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.seeyon.v3x.edoc.constants.EdocNavigationEnum;
import com.seeyon.ctp.util.Datetimes;

public class DateUtil {

    // 获得当前日期与本周日相差的天数
    public static int getMondayPlus() {
        Calendar cd = Calendar.getInstance();
        // 获得今天是一周的第几天，星期日是第一天
        int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
        if(dayOfWeek == 1) {
            return 0;
        } else {
            return 1 - dayOfWeek;
        }
    }

    public static int getYearPlus() {
        Calendar cd = Calendar.getInstance();
        int yearOfNumber = cd.get(Calendar.DAY_OF_YEAR);// 获得当天是一年中的第几天
        cd.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
        cd.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
        int MaxYear = cd.get(Calendar.DAY_OF_YEAR);
        if(yearOfNumber == 1) {
            return -MaxYear;
        } else {
            return 1 - yearOfNumber;
        }
    }

    // 获得本周一的日期
    public static String getMondayOFWeek() {
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus);
        java.util.Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        String preMonday = df.format(monday);
        return preMonday;
    }

    // 获得上周星期日的日期
    public static String getPreviousWeekSunday() {
        int weeks = -1;
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + weeks);
        java.util.Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        String preMonday = df.format(monday);
        return preMonday;
    }

    // 获得上周星期一的日期
    public static String getPreviousWeekday() {
        int weeks = -1;
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
        java.util.Date monday = currentDate.getTime();
        DateFormat df = DateFormat.getDateInstance();
        String preMonday = df.format(monday);
        return preMonday;
    }

    // 获取当月第一天
    public static String getFirstDayOfMonth() {
        String str = "";
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        str = Datetimes.formatDate(lastDate.getTime());
        return str;
    }

    // 上月第一天
    public static String getPreviousMonthFirst() {
    	String str = "";
        Calendar lastDate = Calendar.getInstance();
        lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
        lastDate.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
        str = Datetimes.formatDate(lastDate.getTime());
        return str;
    }

    // 获得上月最后一天的日期
    public static String getPreviousMonthEnd() {
    	String str = "";
        Calendar lastDate = Calendar.getInstance();
        lastDate.add(Calendar.MONTH, -1);// 减一个月
        lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        lastDate.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
        str = Datetimes.formatDate(lastDate.getTime());
        return str;
    }

    // 获得本年第一天的日期
    public static String getCurrentYearFirst() {
    	 Date date = new Date();
         String years = Datetimes.format(date, "yyyy");
         int years_value = Integer.parseInt(years);
         return years_value + "-01-01";
    }

    // 获得上年第一天的日期 *
    public static String getPreviousYearFirst() {
    	Date date = new Date();
        String years = Datetimes.format(date, "yyyy");
        int years_value = Integer.parseInt(years);
        years_value--;
        return years_value + "-01-01";
    }

    // 获得上年最后一天的日期
    public static String getPreviousYearEnd() {
    	Date date = new Date();
        String years = Datetimes.format(date, "yyyy");
        int years_value = Integer.parseInt(years);
        years_value--;
        return years_value + "-12-31";
    }

    /**
     * 根据时间枚举返回条件数组
     * @param timeEnumId 时间枚举id EdocCustomerTypeTimeEnum
     * @return 数组，{0：textfiled，1：textfiled1}
     */
    public static String[] getTimeTextFiledByTimeEnum(int timeEnumId) {
    	 String[] condition = new String[2];
         java.util.Date now = new java.util.Date();
         Calendar cd = Calendar.getInstance();
         switch(timeEnumId) {
             case 1 : // (EdocCustomerTypeTimeEnum.DAY.getKey())
                 condition[0] = Datetimes.formatDate(now);
                 condition[1] = condition[0];
                 break;
             case 2 : // EdocCustomerTypeTimeEnum.YESTERDAY.getKey()
                 cd.add(Calendar.DATE, -1);
                 condition[0] = Datetimes.formatDate(cd.getTime());
                 condition[1] = condition[0];
                 break;
             case 3 : // EdocCustomerTypeTimeEnum.WEEK.getKey()
                 condition[0] = DateUtil.getMondayOFWeek();
                 condition[1] = Datetimes.formatDate(now);
                 break;
             case 4 : // EdocCustomerTypeTimeEnum.LAST_WEEK.getKey()
                 condition[0] = DateUtil.getPreviousWeekday();
                 condition[1] = DateUtil.getPreviousWeekSunday();
                 break;
             case 5 : // EdocCustomerTypeTimeEnum.MONTH.getKey()
                 condition[0] = DateUtil.getFirstDayOfMonth();
                 condition[1] = Datetimes.formatDate(now);
                 break;
             case 6 : // EdocCustomerTypeTimeEnum.LAST_MONTH.getKey()
                 condition[0] = DateUtil.getPreviousMonthFirst();
                 condition[1] = DateUtil.getPreviousMonthEnd();
                 break;
             case 7 : // EdocCustomerTypeTimeEnum.YEAR.getKey()
                 condition[0] = DateUtil.getCurrentYearFirst();
                 condition[1] = Datetimes.formatDate(now);
                 break;
             case 8 : // EdocCustomerTypeTimeEnum.LAST_YEAR.getKey()
                 condition[0] = DateUtil.getPreviousYearFirst();
                 condition[1] = DateUtil.getPreviousYearEnd();
                 break;
         }
         return condition;
    }

    /**
     * 根据查询类型获取相应的开始结束时间
     * @param type 查询类型
     * @return 开始结束时间数组
     */
    public static Date[] getStartEndTime(int type) {
        Date[] Date = new Date[2];
        Date date = new Date();
        if(type == EdocNavigationEnum.RecieveDateType.Today.ordinal()) {
            Date[0] = Datetimes.getTodayFirstTime();
            Date[1] = Datetimes.getTodayLastTime();
        } else if(type == EdocNavigationEnum.RecieveDateType.LastDay.ordinal()) {
            Date lastDay = Datetimes.addDate(date, -1);
            Date[0] = Datetimes.getTodayFirstTime(lastDay);
            Date[1] = Datetimes.getTodayLastTime(lastDay);
        } else if(type == EdocNavigationEnum.RecieveDateType.ThisWeek.ordinal()) {
            Date[0] = Datetimes.getFirstDayInWeek(date);
            Date[1] = Datetimes.getLastDayInWeek(date);
        } else if(type == EdocNavigationEnum.RecieveDateType.LastWeek.ordinal()) {
            Date lastWeek = Datetimes.addDate(date, -7);
            Date[0] = Datetimes.getFirstDayInWeek(lastWeek);
            Date[1] = Datetimes.getLastDayInWeek(lastWeek);
        } else if(type == EdocNavigationEnum.RecieveDateType.ThisMonth.ordinal()) {
            Date[0] = Datetimes.getFirstDayInMonth(date);
            Date[1] = Datetimes.getLastDayInMonth(date);
        } else if(type == EdocNavigationEnum.RecieveDateType.LastMonth.ordinal()) {
            Date lastMonth = Datetimes.addMonth(date, -1);
            Date[0] = Datetimes.getFirstDayInMonth(lastMonth);
            Date[1] = Datetimes.getLastDayInMonth(lastMonth);
        } else if(type == EdocNavigationEnum.RecieveDateType.ThisYear.ordinal()) {
            Date[0] = Datetimes.getFirstDayInYear(date);
            Date[1] = Datetimes.getLastDayInYear(date);
        } else if(type == EdocNavigationEnum.RecieveDateType.LastYear.ordinal()) {
            Date lastYear = Datetimes.addYear(date, -1);
            Date[0] = Datetimes.getFirstDayInYear(lastYear);
            Date[1] = Datetimes.getLastDayInYear(lastYear);
        }
        return Date;
    }
}
