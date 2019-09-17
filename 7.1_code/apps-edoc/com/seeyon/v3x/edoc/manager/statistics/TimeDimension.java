package com.seeyon.v3x.edoc.manager.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.util.Datetimes;

public class TimeDimension implements StrategyInter{

	@Override
	public Map<Object, List<Object>> statistics(HttpServletRequest request, 
			StatParamVO statParam, List<ContentData> statisticsContentList) throws Exception{

		 
		/***时间处理  ***/
		//时间类型为 年
		String yeartype_startyear = request.getParameter("yeartype-startyear");
		String yeartype_endyear = request.getParameter("yeartype-endyear");
		
		//时间类型为季度
		String seasontype_startyear = request.getParameter("seasontype-startyear");
		String seasontype_endyear = request.getParameter("seasontype-endyear");
		String seasontype_startseason = request.getParameter("seasontype-startseason");
		String seasontype_endseason = request.getParameter("seasontype-endseason");
		 
		//时间类型为月
		String monthtype_startyear = request.getParameter("monthtype-startyear");
		String monthtype_endyear = request.getParameter("monthtype-endyear");
		String monthtype_startmonth = request.getParameter("monthtype-startmonth");
		String monthtype_endmonth = request.getParameter("monthtype-endmonth");
		
		//时间类型为日
		String daytype_startday = request.getParameter("daytype-startday");
		String daytype_endday = request.getParameter("daytype-endday");
		
		request.setAttribute("yeartype_startyear", yeartype_startyear);
		request.setAttribute("yeartype_endyear", yeartype_endyear);
		request.setAttribute("seasontype_startyear", seasontype_startyear);
		request.setAttribute("seasontype_endyear", seasontype_endyear);
		request.setAttribute("seasontype_startseason", seasontype_startseason);
		request.setAttribute("seasontype_endseason", seasontype_endseason);
		request.setAttribute("monthtype_startyear", monthtype_startyear);
		request.setAttribute("monthtype_endyear", monthtype_endyear);
		request.setAttribute("monthtype_startmonth", monthtype_startmonth);
		request.setAttribute("monthtype_endmonth", monthtype_endmonth);
		request.setAttribute("daytype_startdate", daytype_startday);
		request.setAttribute("daytype_enddate", daytype_endday);
		
		java.util.Date starttime = null;
		java.util.Date endtime = null;
		
		List<String> times = new ArrayList<String>();
		String[] yearArr = null;
		String[] seasonArr = null;
		String[] monthArr = null;
		String[] dateArr = null;
		
		//时间类型
		int timeType = Integer.parseInt(request.getParameter("timeType")); 
		//时间类型为 年
		if(timeType == 1){   
			
			Date sd = Datetimes.parse(yeartype_startyear,"yyyy");
			starttime = Datetimes.getFirstDayInYear(sd);	
			Date ed = Datetimes.parse(yeartype_endyear,"yyyy");
			endtime = Datetimes.getLastDayInYear(ed);
			
			//从开始时间到结束时间 获得之间的全部时间，用于页面统计表左侧显示 以及 执行hql分组查询后List存入Map处理
			int startyear = Integer.parseInt(yeartype_startyear);
			int endyear = Integer.parseInt(yeartype_endyear);
			while(startyear <= endyear){  
				times.add(String.valueOf(startyear++));
			}
			yearArr = new String[times.size()];
			for(int i=0;i<yearArr.length;i++){
				yearArr[i] = times.get(i);
			}
		}else if(timeType==2){
			int k = 3;
			int startmonth = (Integer.parseInt(seasontype_startseason)-1) * k+1;
			Date sd = Datetimes.parse(seasontype_startyear+"-"+startmonth,"yyyy-MM");
			starttime = Datetimes.getFirstDayInSeason(sd);

			int endmonth = Integer.parseInt(seasontype_endseason) * k;
			Date ed = Datetimes.parse(seasontype_endyear+"-"+endmonth,"yyyy-MM");
			endtime = Datetimes.getLastDayInSeason(ed);
			
			//计算相隔季度数
			int startyear = Integer.parseInt(seasontype_startyear);
			int endyear = Integer.parseInt(seasontype_endyear);
			
			int startseason = Integer.parseInt(seasontype_startseason);
			int endseason = Integer.parseInt(seasontype_endseason);
			
			//当开始年份 小于 结束年份时
			while(startyear < endyear){
				//查询出的值为 2011 1 年和季度之间是一个空格
				String date = startyear+" "+startseason;
				times.add(date);
				startseason++;
				if(startseason>=5){
					startseason = 1;
					startyear++;
				}
			}
			//年份相同时
			while(startseason <= endseason){  
				String date = startyear+" "+startseason;
				startseason++;
				times.add(date);
			}
			yearArr = new String[times.size()];
			seasonArr = new String[times.size()];
			for(int i=0;i<yearArr.length;i++){
				String d = times.get(i);
				String[] dd = d.split(" ");
				yearArr[i] = dd[0];
				seasonArr[i] = dd[1];
			}
		}else if(timeType==3){
			Date sd = Datetimes.parse(monthtype_startyear+"-"+monthtype_startmonth,"yyyy-MM");
			starttime = Datetimes.getFirstDayInMonth(sd);
			
			Date ed = Datetimes.parse(monthtype_endyear+"-"+monthtype_endmonth,"yyyy-MM");
			endtime = Datetimes.getLastDayInMonth(ed);
			
			//计算相隔月数
			int startyear = Integer.parseInt(monthtype_startyear);
			int endyear = Integer.parseInt(monthtype_endyear);
			
			int startmonth = Integer.parseInt(monthtype_startmonth);
			int endmonth = Integer.parseInt(monthtype_endmonth);
			
			//当开始年份 小于 结束年份时
			while(startyear < endyear){
				//这里很重要，1-9月前面要补0，因为数据库中存放的日期数据1-9前是有0的：01,02,03,后面的算法涉及到日期字符串的比对
				String date = startmonth >= 10 ? startyear+"-"+startmonth : startyear+"-0"+startmonth;
				times.add(date);
				startmonth++;
				if(startmonth>=13){
					startmonth = 1;
					startyear++;
				}
			}
			//年份相同时
			while(startmonth <= endmonth){  
				String date = startmonth >= 10 ? startyear+"-"+startmonth : startyear+"-0"+startmonth;
				startmonth++;
				times.add(date);
			}
			yearArr = new String[times.size()];
			monthArr = new String[times.size()];
			for(int i=0;i<yearArr.length;i++){
				String d = times.get(i);
				String[] dd = d.split("-");
				yearArr[i] = dd[0];
				monthArr[i] = dd[1];
			}
		}else if(timeType==4){
			starttime = Datetimes.getTodayFirstTime(daytype_startday);
			endtime = Datetimes.getTodayLastTime(daytype_endday);
			//计算相隔天数
			long minusDay = Datetimes.minusDay(endtime, starttime);
			
			for(int i=0;i<=minusDay;i++){
				String day = Datetimes.formatDate(Datetimes.addDate(starttime,i));
				times.add(day);
			}
			
			dateArr = new String[times.size()];
			for(int i=0;i<dateArr.length;i++){
				dateArr[i] = times.get(i);
			}
		}
		statParam.setStarttime(starttime);
		statParam.setEndtime(endtime);
		statParam.setTimes(times);
		
		request.setAttribute("yearArr", yearArr);
		request.setAttribute("seasonArr", seasonArr);
		request.setAttribute("monthArr", monthArr);
		request.setAttribute("dateArr", dateArr);
		request.setAttribute("timeType", timeType);
		
		
		
		Map<Object, List<Object>> map = new LinkedHashMap<Object, List<Object>>();
		for(ContentData cd : statisticsContentList){
			int contentType = cd.getContentType();
			StatisticsContentTypeEnum typeEnum = StatisticsContentTypeEnum.valueOf(contentType);
			ContentHandler handler = typeEnum.getContentHandlerInstance();
			
			Map<Object, List<Object>> tempMap = handler.statisticsTimeAfterFind(statParam,cd.getContents());
			Iterator it = tempMap.keySet().iterator();
			while(it.hasNext()){
				String key = (String)it.next();
				List<Object> numList = tempMap.get(key);
				if(map.get(key)==null){
					map.put(key, numList);
				}else{
					List<Object> tempList = map.get(key);
					tempList.addAll(numList);
					map.put(key,tempList);
				}
			}
		}
		
		return map;
	}

	
	public static void main(String[] args) {
		List<Object> timeList = new ArrayList<Object>();
		timeList.add("2");timeList.add("3");
		
//		new TimeDimension().statistics(timeList, null, null);
	}
}
