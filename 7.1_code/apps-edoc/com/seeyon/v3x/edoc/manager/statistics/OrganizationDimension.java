package com.seeyon.v3x.edoc.manager.statistics;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.seeyon.ctp.util.Datetimes;

public class OrganizationDimension implements StrategyInter{
	
	@Override
	public Map<Object, List<Object>> statistics(HttpServletRequest request, 
			StatParamVO statParam, List<ContentData> statisticsContentList) throws Exception{

		/***时间处理  ***/
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
		
		//时间类型
        int timeType = Integer.parseInt(request.getParameter("timeType"));  
        
		java.util.Date starttime = null;
		java.util.Date endtime = null;
	
		//时间类型为 年
		if(timeType == 1){   
			Date sd = Datetimes.parse(yeartype_startyear, "yyyy");
			starttime = Datetimes.getFirstDayInYear(sd);	
			Date ed = Datetimes.parse(yeartype_endyear,"yyyy");
			endtime = Datetimes.getLastDayInYear(ed);
		}else if(timeType==2){
			
			int k = 3;
			int startmonth = (Integer.parseInt(seasontype_startseason)-1) * k+1;
			Date sd = Datetimes.parse(seasontype_startyear+"-"+startmonth,"yyyy-MM");
			starttime = Datetimes.getFirstDayInSeason(sd);

			int endmonth = Integer.parseInt(seasontype_endseason) * k;
			Date ed = Datetimes.parse(seasontype_endyear+"-"+endmonth,"yyyy-MM");
			endtime = Datetimes.getLastDayInSeason(ed);

		}else if(timeType==3){
			
			Date sd = Datetimes.parse(monthtype_startyear+"-"+monthtype_startmonth,"yyyy-MM");
			starttime = Datetimes.getFirstDayInMonth(sd);
			
			Date ed = Datetimes.parse(monthtype_endyear+"-"+monthtype_endmonth,"yyyy-MM");
			endtime = Datetimes.getLastDayInMonth(ed);
			
		}else if(timeType==4){
			
			starttime = Datetimes.getTodayFirstTime(daytype_startday);
			endtime = Datetimes.getTodayLastTime(daytype_endday);
			
			String[] date = daytype_startday.split("-");
			String daytype_startyear = date[0];
			String daytype_startmonth = date[1];
			String daytype_startdays = date[2]; 
			request.setAttribute("daytype_startyear", daytype_startyear);
			int sm = Integer.parseInt(daytype_startmonth);
			String smonth = sm < 10 ? daytype_startmonth.substring(1) : daytype_startmonth;
			request.setAttribute("daytype_startmonth", smonth);
			request.setAttribute("daytype_startday", daytype_startdays);
			
			String[] date2 = daytype_endday.split("-");
			String daytype_endyear = date2[0];
			String daytype_endmonth = date2[1];
			String daytype_enddays = date2[2]; 
			request.setAttribute("daytype_endyear", daytype_endyear);
			int sm2 = Integer.parseInt(daytype_endmonth);
			String smonth2 = sm2 < 10 ? daytype_endmonth.substring(1) : daytype_endmonth;
			request.setAttribute("daytype_endmonth", smonth2);
			request.setAttribute("daytype_endday", daytype_enddays);
			
			
		}
		statParam.setStarttime(starttime);
		statParam.setEndtime(endtime);
		
		
		Map<Object, List<Object>> map = new LinkedHashMap<Object, List<Object>>();
		for(ContentData cd : statisticsContentList){
			int contentType = cd.getContentType();
			StatisticsContentTypeEnum typeEnum = StatisticsContentTypeEnum.valueOf(contentType);
			ContentHandler handler = typeEnum.getContentHandlerInstance();
			
			Map<Object, List<Object>> tempMap = handler.statisticsTimeAfterFind(statParam,cd.getContents());
			Iterator it = tempMap.keySet().iterator();
			while(it.hasNext()){
				long key = (Long)it.next();
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

}
