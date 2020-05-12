package com.seeyon.cap4.form.modules.engin.formula;

import com.seeyon.cap4.form.bean.*;
import com.seeyon.cap4.form.modules.engin.formula.FormulaEnums.ConditionSymbol;
import com.seeyon.cap4.form.modules.engin.formula.validate.FormulaValidate;
import com.seeyon.cap4.form.modules.serialNumber.CAP4SerialNumberManager;
import com.seeyon.cap4.form.service.CAP4FormManager;
import com.seeyon.cap4.form.util.Enums.FieldType;
import com.seeyon.cap4.form.util.FormConstant;
import com.seeyon.cap4.form.util.StringUtils;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumItem;
import com.seeyon.ctp.form.po.FormCustomFunction;
import com.seeyon.ctp.organization.OrgConstants.MemberPostType;
import com.seeyon.ctp.organization.bo.V3xOrgDepartment;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DateUtil;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.StringUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.ctp.workflow.script.WorkFlowFunctions;
import com.seeyon.v3x.worktimeset.manager.WorkTimeManager;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定义公式运算函数
 *
 * @author wangfeng
 */
public class FormulaFunction {
    public static final String FormulaFunction_contextKey1 = "FormulaFunction_contextKey1";
    private static final String FormulaFunction_contextKey2 = "FormulaFunction_contextKey2";
    private static final Log LOGGER = CtpLogFactory.getLog(FormulaFunction.class);
    private static ConcurrentMap<String, DecimalFormat> decimalFormats = new ConcurrentHashMap<String, DecimalFormat>();
    private static Pattern prePattern = Pattern.compile("preRow\\(\\'\\w+\\'\\)");
    private static Pattern fieldPattern = Pattern.compile("(field\\d{4,})");
    private static BigDecimal oneDay = new BigDecimal(1000 * 60 * 60 * 24);
    /**
     * @param obj 需要求和的list
     * @return 结果
     */
    public static BigDecimal sum(Object obj) {
        BigDecimal result = new BigDecimal("0");
        if (obj == null) {
            return result;
        }
        List<Object> list = new ArrayList<Object>();
        if (obj instanceof List) {
            //noinspection unchecked
            list = (List<Object>) obj;
        } else {
            list.add(obj);
        }
        if (Strings.isEmpty(list)) {
            return result;
        } else {
            for (Object object : list) {
                if (object instanceof String) {
                    BigDecimal tem = null;
                    //noinspection EmptyCatchBlock
                    try {
                        tem = new BigDecimal((String) object);
                    } catch (Exception e) {
                    	LOGGER.error(e.getMessage(), e);
                    }
                    if (tem != null) {
                        result = result.add(tem);
                    }
                } else if (object instanceof Double) {
                    result = result.add(new BigDecimal((Double) object));
                } else if (object instanceof Long) {
                    result = result.add(new BigDecimal((Long) object));
                } else if (object instanceof Integer) {
                    result = result.add(new BigDecimal((Integer) object));
                } else if (object instanceof BigDecimal) {
                    result = result.add((BigDecimal) object);
                }
            }
        }
        return result;
    }

    /**
     * @param obj 需要求平均值的list
     * @return 结果
     */
    public static BigDecimal aver(Object obj) {
        BigDecimal result = new BigDecimal("0");
        if (obj == null) {
            return result;
        }
        List<Object> list = new ArrayList<Object>();
        if (obj instanceof List) {
            //noinspection unchecked
            list = (List<Object>) obj;
        } else {
            list.add(obj);
        }
        BigDecimal summ = sum(list);
        if (summ != null) {
            return summ.divide(new BigDecimal(list.size()), 10, BigDecimal.ROUND_HALF_EVEN);
        } else {
            return result;
        }

    }

    /**
     * 如果对于all和eist有重复表上一行验证
     * @param functionName
     * @param expression
     * @param formMap
     * @param chongfuxiangKeyList
     * @param chongfuxiangValueList
     * @param tempContext
     * @return
     * @throws BusinessException
     */
    private static boolean validatePreRow(String functionName, String expression, Map<String, Object> formMap,
        List<String> chongfuxiangKeyList, List<List<Object>> chongfuxiangValueList, Map<String, Object> tempContext ) throws BusinessException {
        String field = "";
        Matcher matcher = prePattern.matcher(expression.replaceAll(" ", ""));
        while (matcher.find()) {
            field = matcher.group();
        }
        boolean result = true;
        int chongfuxiangKeyLength = chongfuxiangKeyList.size();
        FormBean fb = (FormBean) formMap.get("formBean");
        Long formId = formMap.get("formId") == null ? 0L :Long.parseLong(String.valueOf(formMap.get("formId")));
        if (fb == null) {
            CAP4FormManager cap4FormManager = (CAP4FormManager) AppContext.getBean("cap4FormManager");
            fb = cap4FormManager.getEditingForm(formId);
        }
        FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
        if (cacheMasterData != null) {
            FormFieldBean fieldBean = fb.getFieldBeanByName(field.substring(8, field.indexOf("')")).trim());
            List<FormDataSubBean> subBeans = cacheMasterData.getSubData(fieldBean.getOwnerTableName());
            for (int i = 0, k = subBeans.size(); i < k; i++) {
                for (int j = 0; j < chongfuxiangKeyLength; j++) {
                    if (chongfuxiangValueList.get(j).size() > i) {
                        tempContext.put(chongfuxiangKeyList.get(j), chongfuxiangValueList.get(j).get(i));
                        if (i > 0) {
                            tempContext.put(chongfuxiangKeyList.get(j)+"_pre", chongfuxiangValueList.get(j).get(i-1));
                        }
                    }
                }
                if (i > 0) {
                    result = FormulaUtil.isMatch(expression, tempContext, FormulaFunction_contextKey2);
                }
                if ("all".equals(functionName)) {
                    if (!result) {
                        break;
                    }
                } else {
                    if ((k == 0 && result) || (k > 0 && i != 0 && result)) {
                        break;//(k == 0 && result) || (k > 0 && i != 0 && result)
                    }
                }
            }
        }
        return result;
    }

    /**
     * 重复项所有行
     *
     * @param expression
     * @return
     * @throws BusinessException
     */
    public static boolean all(String expression) throws BusinessException {
        //expression是一个真正的表达式，使用Groovy引擎解析的话，有了它，还需要一个Map类替换表达式中的变量
        //由于重复项会有一个或多个，所以必须遍历多次，构造多个Map
        boolean result = true;
        //Map<String, Object> formMap = ScriptEvaluator.getInstance().getContext();
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null && formMap.size() > 0) {
            //用于表达式计算的Map
            Map<String, Object> tempContext = new HashMap<String, Object>();
            //重复项key列表
            List<String> chongfuxiangKeyList = new ArrayList<String>();
            //重复项value列表
            List<List<Object>> chongfuxiangValueList = new ArrayList<List<Object>>();
            int maxLen = 0;
            //遍历form数据，找到最大长度的那个重复项，并且将非重复项数据直接填入Map中，将重复项的key和value分别放入两个List中
            for (Map.Entry<String, Object> entry : formMap.entrySet()) {
                if (entry.getValue() instanceof List) {
                    chongfuxiangKeyList.add(entry.getKey());
                    @SuppressWarnings("unchecked")
                    List<Object> objectList = (List<Object>) entry.getValue();
                    if (objectList != null && objectList.size() > 0) {
                        if (objectList.size() > maxLen) {
                            maxLen = objectList.size();
                        }
                        chongfuxiangValueList.add(objectList);
                    }
                } else {
                    tempContext.put(entry.getKey(), entry.getValue());
                }
            }
            int chongfuxiangKeyLength = chongfuxiangKeyList.size();
            if(expression.replaceAll(" ","").indexOf(FormulaEnums.FunctionSymbol.preRow.getKey()+"(") > -1) {
                List<String> conditions = FormulaValidate.parseToSimpleConditions(expression.replaceAll("\\&\\&"," and "));
                for (String condition : conditions) {
                    if(!result) {
                        break;
                    }
                    if (condition.replaceAll(" ", "").indexOf(FormulaEnums.FunctionSymbol.preRow.getKey() + "(") > -1) {
                        result = validatePreRow("all", condition, formMap, chongfuxiangKeyList, chongfuxiangValueList, tempContext);
                    }else{
                        for (int i = 0; i < maxLen; i++) {
                            //将重复项数据中的一个单项放入Map
                            for (int j = 0; j < chongfuxiangKeyLength; j++) {
                                if (chongfuxiangValueList.get(j).size() > i) {
                                    tempContext.put(chongfuxiangKeyList.get(j), chongfuxiangValueList.get(j).get(i));
                                }
                            }
                            result = FormulaUtil.isMatch(condition, tempContext, FormulaFunction_contextKey2);
                            if (!result) {
                                break;
                            }
                        }
                    }
                }
            } else {
                //表达式需要计算maxLen次
                for (int i = 0; i < maxLen; i++) {
                    //将重复项数据中的一个单项放入Map
                    for (int j = 0; j < chongfuxiangKeyLength; j++) {
                        if (chongfuxiangValueList.get(j).size() > i) {
                            tempContext.put(chongfuxiangKeyList.get(j), chongfuxiangValueList.get(j).get(i));
                        }
                    }
                    result = FormulaUtil.isMatch(expression, tempContext, FormulaFunction_contextKey2);
                    if (!result) {
                        break;
                    }
                }
            }
        } else {
            throw new BusinessException("WorkFlow:表单数据不存在，expression=" + expression);
        }
        return result;
    }

    /**
     * 重复项单元格中是否有一行满足所需的条件表达式
     *
     * @param expression
     * @return
     */
    public static boolean exist(String expression) throws BusinessException {
        //expression是一个真正的表达式，使用Groovy引擎解析的话，有了它，还需要一个Map类替换表达式中的变量
        //由于重复项会有一个或多个，所以必须遍历多次，构造多个Map
        boolean result = false;
        //Map<String, Object> formMap = ScriptEvaluator.getInstance().getContext();
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null && formMap.size() > 0) {
            //用于表达式计算的Map
            Map<String, Object> tempContext = new HashMap<String, Object>();
            //重复项key列表
            List<String> chongfuxiangKeyList = new ArrayList<String>();
            //重复项value列表
            List<List<Object>> chongfuxiangValueList = new ArrayList<List<Object>>();
            int maxLen = 0;
            //遍历form数据，找到最大长度的那个重复项，并且将非重复项数据直接填入Map中，将重复项的key和value分别放入两个List中
            for (Map.Entry<String, Object> entry : formMap.entrySet()) {
                if (entry.getValue() instanceof List) {
                    chongfuxiangKeyList.add(entry.getKey());
                    @SuppressWarnings("unchecked")
                    List<Object> objectList = (List<Object>) entry.getValue();
                    if (objectList != null && objectList.size() > 0) {
                        if (objectList.size() > maxLen) {
                            maxLen = objectList.size();
                        }
                        chongfuxiangValueList.add(objectList);
                    }
                } else {
                    tempContext.put(entry.getKey(), entry.getValue());
                }
            }
            //表达式需要计算maxLen次
            int chongfuxiangKeyLength = chongfuxiangKeyList.size();
            for (int i = 0; i < maxLen; i++) {
                //将重复项数据中的一个单项放入Map
                for (int j = 0; j < chongfuxiangKeyLength; j++) {
                    if (chongfuxiangValueList.get(j).size() > i) {
                        tempContext.put(chongfuxiangKeyList.get(j), chongfuxiangValueList.get(j).get(i));
                        if (i > 0) {
                            tempContext.put(chongfuxiangKeyList.get(j)+"_pre", chongfuxiangValueList.get(j).get(i-1));
                        }
                    }
                }
                if (expression.replaceAll(" ","").indexOf(FormulaEnums.FunctionSymbol.preRow.getKey()+"(") > -1 && i == 0) {
                    List<String> conditions = FormulaValidate.parseToSimpleConditions(expression.replaceAll("\\&\\&"," and "));
                    result = maxLen == 1 ? true : false;
                    if (maxLen == 1) {
                        for (String condition : conditions) {
                            if (condition.replaceAll(" ", "").indexOf(FormulaEnums.FunctionSymbol.preRow.getKey() + "(") == -1) {
                                result = FormulaUtil.isMatch(condition, tempContext, FormulaFunction_contextKey2);
                            }
                        }
                    }
                }else {
                    result = FormulaUtil.isMatch(expression, tempContext, FormulaFunction_contextKey2);
                }
                if (result) {
                    break;
                }
            }
        } else {
            throw new BusinessException("WorkFlow:表单数据不存在，expression=" + expression);
        }
        return result;
    }

    /**
     * 判断重复表所有行的某列是否唯一
     *
     * @param obj
     * @return
     */
    public static boolean unique(Object obj) {
        List<Object> list = getValidData(obj);
        if (list == null || list.size() == 0) {
            return true;
        }
        Set<Object> set = new HashSet<Object>();
        for (Object o : list) {
            set.add(o);
        }
        return list.size() == set.size();
    }

    /**
     * 重复表最早
     *
     * @param obj
     * @return
     */
    public static Object earliest(Object obj) {
        Object returnValue = null;
        List<Object> list = getValidData(obj);
        if (list==null||list.size() == 0) {
            return null;
        }
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (i == 0) {
                returnValue = list.get(i);
            } else {
                Object o = list.get(i);
                if (compareDate("<", o, returnValue)) {
                    returnValue = o;
                }
            }
        }
        return returnValue;
    }

    /**
     * 重复表最晚
     *
     * @param obj
     * @return
     */
    public static Object latest(Object obj) {
        Object returnValue = null;
        List<Object> list = getValidData(obj);
        if (list == null || list.size() == 0) {
            return null;
        }
        int len = list.size();
        for (int i = 0; i < len; i++) {
            if (i == 0) {
                returnValue = list.get(i);
            } else {
                Object o = list.get(i);
                if (compareDate(">", o, returnValue)) {
                    returnValue = o;
                }
            }
        }
        return returnValue;
    }

    /**
     * 排除list中的空值
     *
     * @param obj
     * @return
     */
    private static List<Object> getValidData(Object obj) {
        List<Object> list = new ArrayList<Object>();
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            List<Object> tempList = (List<Object>) obj;
            for(Object o:tempList){
                if (o != null && !"".equals(String.valueOf(o))) {
                    list.add(o);
                }
            }
        } else {
            if (!"".equals(String.valueOf(obj))) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 日期比较函数
     * 边际值匹配情况如下所示
     * 日期类型字段   等于(空字符串，没有空格)   不等于(空字符串，没有空格)  大于(空字符串，没有空格)   大于等于(空字符串，没有空格) 小于(空字符串，没有空格)   小于等于(空字符串，没有空格)
     * 有值（例如"2013-3-15 "）  false               true                           false                       false                           false                       false
     * 无值（例如""，没有空格）   true                false                           false                       true                            false                       true
     *
     * @param operator
     * @param dateObj1
     * @param dateObj2
     * @return
     */
    public static boolean compareDate(String operator, Object dateObj1, Object dateObj2) {
        return WorkFlowFunctions.compareDate(operator, dateObj1, dateObj2);
    }

    /**
     * 转换函数， GROOVY不支持字符串直接对比
     *
     * @param dateStr
     * @return 日期对象
     * @throws BusinessException
     */
    public static Date to_date(String dateStr) throws BusinessException {
        if (StringUtil.checkNull(dateStr)) {
            return null;
        }
        Date d = null;
        try {
            d = DateUtil.parse(dateStr);
        } catch (ParseException pe) {
            throw new BusinessException(pe);
        }
        return d;
    }

    /**
     * 日期差
     *
     * @param o1 日期
     *           <li>日期差格式 yyyy-MM-dd</li>
     * @param o2 日期
     * @return
     * @throws BusinessException 当日起时间字符串格式不匹配时
     */
    public static double differDate(Object o1, Object o2) throws BusinessException {
        if (o1 == null || o2 == null) {
            return 0d;
        }
        Object date1 = o1;
        Object date2 = o2;
        if (date1 instanceof Date) {
            Date newName = (Date) date1;
            date1 = DateUtil.format(newName, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (date2 instanceof Date) {
            Date newName = (Date) date2;
            date2 = DateUtil.format(newName, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (date1 instanceof String) {
            if (Strings.isBlank(String.valueOf(date1))) {
                return 0d;
            }
            date1 = DateUtil.toDate((String) date1, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (date2 instanceof String) {
            if (Strings.isBlank(String.valueOf(date2))) {
                return 0d;
            }
            date2 = DateUtil.toDate((String) date2, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (!(date1 instanceof Date) || !(date2 instanceof Date)) {
            return 0d;
        }
        long differ = ((Date) date1).getTime() - ((Date) date2).getTime();
        BigDecimal dbigNum = new BigDecimal(differ);
        return dbigNum.divide(oneDay,5,BigDecimal.ROUND_HALF_UP).doubleValue();
        //return dbigNum.divide(oneDay).doubleValue();
    }

    /**
     * 计算工作日期差
     *
     * @param o1
     * @param o2
     * @return
     */
    public static double differDateByWorkDay(Object o1, Object o2) throws BusinessException {
        User currentUser = AppContext.getCurrentUser();
        if (currentUser == null) {
            return 0d;
        }
        if (o1 == null || o2 == null) {
            return 0d;
        }
        Object date1 = o1;
        Object date2 = o2;
        if (date1 instanceof String) {
            if (Strings.isBlank(String.valueOf(date1))) {
                return 0d;
            }
            date1 = DateUtil.toDate((String) date1, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (date2 instanceof String) {
            if (Strings.isBlank(String.valueOf(date2))) {
                return 0d;
            }
            date2 = DateUtil.toDate((String) date2, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (!(date1 instanceof Date) || !(date2 instanceof Date)) {
            return 0d;
        }
        WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
        long differ = workTimeManager.getDealWithTimeValue(Datetimes.getTodayFirstTime((Date) date2), Datetimes.getTodayLastTime((Date) date1), currentUser.getLoginAccount());
        differ = differ / 60000;
        double workTimeOfDay = getWorkTimeOfDay();// 工作日一天是多少分钟
        double workDay = differ / workTimeOfDay;
        return workDay;
    }

    /**
     * 日期时间差
     *
     * @param o1 日期时间 格式 yyyy-MM-dd HH:mm
     * @param o2 日期时间格式 yyyy-MM-dd HH:mm
     * @return
     * @throws BusinessException
     */
    public static double differDateTime(Object o1, Object o2) throws BusinessException {
        if (o1 == null || o2 == null) {
            return 0d;
        }
        Object date1 = o1;
        Object date2 = o2;
        if (date1 instanceof String) {
            if (Strings.isBlank(String.valueOf(date1))) {
                return 0d;
            }
            date1 = DateUtil.toDate((String) date1, DateUtil.YMDHMS_PATTERN);
        }
        if (date2 instanceof String) {
            if (Strings.isBlank(String.valueOf(date2))) {
                return 0d;
            }
            date2 = DateUtil.toDate((String) date2, DateUtil.YMDHMS_PATTERN);
        }
        if (!(date1 instanceof Date) || !(date2 instanceof Date)) {
            return 0d;
        }
        long differ = ((Date) date1).getTime() - ((Date) date2).getTime();

        return (double) differ / (double) (1000 * 60 * 60 * 24);
    }

    /**
     * 工作日期时间差
     *
     * @param o1
     * @param o2
     * @return
     */
    public static double differDateTimeByWorkDay(Object o1, Object o2) throws BusinessException {
        User currentUser = AppContext.getCurrentUser();
        if (currentUser == null) {
            return 0d;
        }
        if (o1 == null || o2 == null) {
            return 0d;
        }
        Object date1 = o1;
        Object date2 = o2;
        if (date1 instanceof String) {
            if (Strings.isBlank(String.valueOf(date1))) {
                return 0d;
            }
            date1 = DateUtil.toDate((String) date1, DateUtil.YMDHMS_PATTERN);
        }
        if (date2 instanceof String) {
            if (Strings.isBlank(String.valueOf(date2))) {
                return 0d;
            }
            date2 = DateUtil.toDate((String) date2, DateUtil.YMDHMS_PATTERN);
        }
        if (!(date1 instanceof Date) || !(date2 instanceof Date)) {
            return 0d;
        }
        WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
        long differ = workTimeManager.getDealWithTimeValue((Date) date2, (Date) date1, currentUser.getLoginAccount());
        differ = differ / 60000;
        double workTimeOfDay = getWorkTimeOfDay();// 工作日一天是多少分钟
        double workDay = differ / workTimeOfDay;
        return workDay;
    }

    /**
     * 天小时分显示格式
     *
     * @param d
     * @return
     */
    public static String formatDayHourMinus(Double d) {
        Double differ = d;
        int diffDay = differ.intValue();
        differ = (differ - diffDay) * 24;
        int diffHour = differ.intValue();
        differ = (differ - diffHour) * 60;
        long diffMinus = Math.round(differ);
        return getFormatDataString(diffDay, diffHour, diffMinus);
    }

    public static String formatDayHourMinus(BigDecimal differ) {
        return formatDayHourMinus(differ.doubleValue());
    }

    /**
     * 天小时分显示格式(工作日)
     *
     * @param d
     * @return
     */
    public static String formatWorkDayHourMinus(Double d) {
        double workTimeOfDay = getWorkTimeOfDay();// 工作日一天是多少分钟
        double workHourOfDay = (workTimeOfDay / (60));//工作日一天是多少小时
        double workMinuteOfHour = 60;//工作日一小时是多少分钟
        Double differ = d;
        int diffDay = differ.intValue();
        differ = (differ - diffDay) * workHourOfDay;
        int diffHour = differ.intValue();
        differ = (differ - diffHour) * workMinuteOfHour;
        long diffMinus = Math.round(differ);
        return getFormatDataString(diffDay, diffHour, diffMinus);
    }

    public static String formatWorkDayHourMinus(BigDecimal differ) {
        return formatWorkDayHourMinus(differ.doubleValue());
    }

    /**
     * 本方法主要是在double乘除运算之后防止出现 0小时60分钟的情况。
     * 传入天、时、分后获取 ×天×小时×分
     *
     * @param day
     * @param hour
     * @param minus
     * @return
     */
    private static String getFormatDataString(int day, int hour, long minus) {
        //diffMinus在计算天数的时候被换算成double类型天数后此处又还原成分钟数，中间会产生很小的变化
        //原来60分此处会出现59.997...这样的数值，round后为60分 显示出 x小时60分的形式，此处加上换算。
        int diffDay = day;
        int diffHour = hour;
        long diffMinus = minus;
        if (diffMinus == 60) {
            diffHour = diffHour + 1;
            diffMinus = 0;
            if (diffHour == 24) {
                diffDay = diffDay + 1;
                diffHour = 0;
            }
        }
        //还有可能出现 -1天-1小时-60分的情况
        if (diffMinus == -60) {
            diffHour = diffHour - 1;
            diffMinus = 0;
            if (diffHour == -24) {
                diffDay = diffDay - 1;
                diffHour = 0;
            }
        }
        String dateStr = diffDay + "天" + diffHour + "小时" + diffMinus + "分";
        if (diffDay < 0 || diffHour < 0 || diffMinus < 0) {
            dateStr = "-" + Math.abs(diffDay) + "天" + Math.abs(diffHour) + "小时" + Math.abs(diffMinus) + "分";
        }
        return dateStr;
    }

    /*public static void main(String[] args) throws BusinessException {
        String s = "safdd(d)(s)";
        System.out.println(s.substring(0, s.lastIndexOf("(")));
        System.out.println(differDate("2014-12-29 11:26:00", "2014-12-29"));
    }*/

    /**
     * 天显示格式
     *
     * @param differ 日期差
     * @param intDigit 小数位数
     * @return String
     */
    public static String formatDay(Double differ, int intDigit) {
        BigDecimal bd = new BigDecimal(differ);
        //处理小数位
        DecimalFormat decimalFmt1 = FormulaFunction.getDecimalFormat(intDigit, Double.doubleToRawLongBits(differ) == 0 ? "0" : "");
        return decimalFmt1.format(bd) + "天";
    }

    public static String formatDay(BigDecimal differ, int intDigit) {
        return formatDay(differ.doubleValue(), intDigit);
    }

    /**
     * 天显示格式(工作日)
     *
     * @param differ
     * @return
     */
    public static String formatWorkDay(Double differ, int intDigit) {
        return formatDay(differ, intDigit);
    }

    public static String formatWorkDay(BigDecimal differ, int intDigit) {
        return formatWorkDay(differ.doubleValue(), intDigit);
    }

    /**
     * 获取工作日是多少分钟
     *
     * @return
     */
    private static Integer getWorkTimeOfDay() {
        User currentUser = AppContext.getCurrentUser();
        Integer workTimeOfDay = Integer.valueOf(0);
        Calendar cal = Calendar.getInstance();
        WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
        try {
            workTimeOfDay = workTimeManager.getEachDayWorkTime(cal.get(1), currentUser.getLoginAccount());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return workTimeOfDay;
    }

    /**
     * 日期 加减 一个数字(天)
     *
     * @param date 日期
     * @param num  天
     * @return
     * @throws BusinessException
     */
    public static Date calcDate(Object date, Object num) throws BusinessException {
        int day = (int) Double.parseDouble(String.valueOf(num));
        if (date instanceof String) {
            return DateUtil.addDay(DateUtil.toDate((String) date, "yyyy-MM-dd"), day);
        } else if (date instanceof Date) {
            return DateUtil.addDay((Date) date, day);
        }
        return null;
    }

    /**
     * 日期时间 加减 一个数字(天)
     *
     * @param date 日期
     * @param num  天
     * @return
     * @throws BusinessException
     */
    public static Date calcDateTime(Object date, Object num) throws BusinessException {
        int day = (int) Double.parseDouble(String.valueOf(num));
        if (date instanceof String) {
            return DateUtil.addDay(DateUtil.toDate((String) date, "yyyy-MM-dd HH:mm:ss"), day);
        } else if (date instanceof Date) {
            return DateUtil.addDay((Date) date, day);
        }
        return null;
    }

    /**
     * 日期+ - 数字(天)(按工作日计算)
     *
     * @param d1 日期
     * @param n  天
     * @return
     * @throws BusinessException
     */
    public static Date calcDateByWorkDay(Object d1, Object n) throws BusinessException {
        if (StringUtil.checkNull(String.valueOf(d1))) {
            return null;
        } else {
            Object date = d1;
            Object num = n;
            if (StringUtil.checkNull(String.valueOf(num))) {
                num = 0d;
            }
            if (date instanceof String) {
                if (num instanceof Double) {
                    return calcDateByWorkDay(DateUtil.toDate((String) date, "yyyy-MM-dd HH:mm:ss"), num);
                } else {
                    return calcDateByWorkDay(DateUtil.toDate((String) date, "yyyy-MM-dd HH:mm:ss"), Double.valueOf(String.valueOf(num)));
                }
            } else if (date instanceof Date) {
                if (num instanceof Double) {
                    Double d = (Double) num;
                    if (Double.doubleToRawLongBits(d) == 0) {
                        return (Date) date;
                    }
                    String operation = "";
                    String dStr = DateUtil.format((Date) date, DateUtil.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND_PATTERN);
                    boolean isDateTime = !(dStr.endsWith("00:00:00"));
                    if (d < 0) {
                        operation = "-";
                        if (!isDateTime) {
                            date = Datetimes.getTodayLastTime((Date) date);
                        }
                    } else {
                        operation = "+";
                        if (!isDateTime) {
                            date = Datetimes.getTodayFirstTime((Date) date);
                        }
                    }
                    long times = Math.abs(d.longValue());
                    WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
                    LOGGER.info("userId: " + AppContext.getCurrentUser().getId() + " loginAccountId:" + AppContext.getCurrentUser().getLoginAccount());
                    return workTimeManager.getComputeDate((Date) date, operation, times, "day", AppContext.getCurrentUser().getLoginAccount());
                } else {
                    return calcDateByWorkDay(date, Double.valueOf(String.valueOf(num)));
                }
            } else {
                return null;
            }
        }
    }

    /**
     * 日期时间+ - 数字(天)(按工作日计算)
     *
     * @param date 日期时间
     * @param n  天
     * @return
     * @throws BusinessException
     */
    public static Date calcDateTimeByWorkDay(Object date, Object n) throws BusinessException {
        //return calcDateByWorkDay(date, num);
        if (StringUtil.checkNull(String.valueOf(date))) {
            return null;
        } else {
            Object num = n;
            if (num == null) {
                num = 0d;
            }
            if (date instanceof String) {
                if (num instanceof Double) {
                    return calcDateByWorkDay(DateUtil.toDate((String) date, "yyyy-MM-dd HH:mm:ss"), num);
                } else {
                    return calcDateByWorkDay(DateUtil.toDate((String) date, "yyyy-MM-dd HH:mm:ss"), Double.valueOf(String.valueOf(num)));
                }
            } else if (date instanceof Date) {
                //按照工作日计算日期   2013-01-01 +  1  = 2013-01-01
                //（计算是当天开始工作时间+天数×当天工作毫秒=当天工作结束时间 及 2013-01-01 8：00 + 1 = 2013-01-01 18：00  日期部分相等）
                //解决1：从2013-01-01 8：00 需要加1毫秒 开始计算 计算结果变成第二个工作日第一毫秒
                //注释掉。应该接口workTimeManager#getComputeDate中修改。
                long value = 0L;
                if (num instanceof Integer) {
                    Integer d = (Integer) num;
                    value = d.longValue();
                } else if (num instanceof Double) {
                    Double d = (Double) num;
                    value = d.longValue();
                }
                String operation = "";
                if (value < 0) {
                    operation = "-";
                } else {
                    operation = "+";
                }
                long times = Math.abs(value);
                WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
                return workTimeManager.getComputeDate((Date) date, operation, times, "day", AppContext.getCurrentUser().getLoginAccount());
            } else {
                return null;
            }
        }
    }

    /**
     * 日期时间+ - 数字(小时)
     *
     * @param date 日期时间字符串 格式 yyyy-MM-dd HH:mm:ss
     * @param num     正数则增加，负数则减去相应小时数
     * @return
     * @throws BusinessException
     */
    public static Date calcDateTimeByHour(Object date, double num) throws BusinessException {
        if (StringUtil.checkNull(String.valueOf(date))) {
            return null;
        } else {
            Date d = null;
            if (date instanceof String) {
                d = DateUtil.toDate(String.valueOf(date), DateUtil.YMDHMS_PATTERN);
            } else if (date instanceof Date) {
                d = (Date) date;
            }
            assert d != null;
            long result = d.getTime();
            result = (long) (result + num * 60 * 60 * 1000);
            Date tempDate = new Date(result);
            tempDate.setTime(result);
            return tempDate;
        }
    }

    /**
     * 工作日期时间+-数字(小时)
     *
     * @param date
     * @param num
     * @return
     * @throws BusinessException
     */
    public static Date calcDateTimeByWorkDayHour(Object date, double num) throws BusinessException {
        if (StringUtil.checkNull(String.valueOf(date))) {
            return null;
        } else {
            Date d = null;
            if (date instanceof String) {
                d = DateUtil.toDate(String.valueOf(date), DateUtil.YMDHMS_PATTERN);
            } else if (date instanceof Date) {
                d = (Date) date;
            }
            String operation = "";
            if (num < 0) {
                operation = "-";
            } else {
                operation = "+";
            }
            User currentUser = AppContext.getCurrentUser();
            if (currentUser == null) {
                return null;
            }
            Date reDate = null;
            long times = (long) num;
            WorkTimeManager workTimeManager = (WorkTimeManager) AppContext.getBean("workTimeManager");
            reDate = workTimeManager.getComputeDate(d, operation, Math.abs(times), "hour", currentUser.getLoginAccount());
            return reDate;
        }
    }

    /**
     * 转换为大写长格式
     *
     * @param currencyDigits 需要转换的数值，最大不超过99999999999.99
     * @return
     * @throws BusinessException
     */
    public static String toUpperForLong(String currencyDigits) throws BusinessException {
        return toUpper(currencyDigits, true, 0);
    }


    /**
     * 转换为大写短格式
     *
     * @param currencyDigits 需要转换的数值，最大不超过99999999999.99
     * @return
     * @throws BusinessException
     */
    public static String toUpperForShort(String currencyDigits) throws BusinessException {
        return toUpper(currencyDigits, false, 0);
    }

    public static String toUpper(String o) throws BusinessException {
        String currencyDigits = o;
        if (StringUtil.checkNull(String.valueOf(0))) {
            currencyDigits = "0";
        }
        return toUpper(currencyDigits, false, 1);
    }

    public static BigDecimal toUpper4Number(Object obj) throws BusinessException {
        //BUG_普通_V5_V6.0sp1_一星卡_福建匹克房地产开发有限公司惠安分公司_数字转换大写，如果为空不转换
        //新需求，值为空不转换
        if(obj==null){
            return null;
        }
        if (obj instanceof Number) {
            Number number = (Number) obj;
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(0);
    }

    /**
     * 为大小写转换一个表达式
     *
     * @param formulaStr
     * @return
     */
    private static void changeDataMap4Upper4Number(String formulaStr, Map<String, Object> dataMap) {
        StringBuffer newFormula = new StringBuffer();
        Matcher matcher = fieldPattern.matcher(formulaStr);
        FormDataBean dataBean = (FormDataMasterBean) dataMap.get("formDataBean");
        FormBean form = (FormBean) dataMap.get("formBean");
        Boolean hasNullField = false;
        dataMap.put("hasNullField",hasNullField);
        if (dataBean == null || form == null) {
            return;
        }
        EnumManager enumManager = (EnumManager) AppContext.getBean("enumManagerNew");
        while (matcher.find()) {
            String group1 = matcher.group(1).trim();
            FormFieldBean fieldBean = form.getFieldBeanByName(group1);
            if (fieldBean == null) {
                return;
            }
            if (!fieldBean.isMasterField()) {
                dataBean = (FormDataBean) dataMap.get("subDataBean");
                if (dataBean == null) {
                    return;
                }
            }
            Object oldValue = dataBean.getFieldValue(group1);
            Object newValue = null;
            if (oldValue != null && Strings.isNotBlank(oldValue.toString())) {
                String str = oldValue.toString();
                //如果是枚举，则取枚举值来 做转换
                if(FormFieldComEnum.RADIO.getKey().equals(fieldBean.getFinalInputType())
                        || FormFieldComEnum.SELECT.getKey().equals(fieldBean.getFinalInputType())){
                    CtpEnumItem item = null;
                    try {
                        item = enumManager.getCtpEnumItem(Long.parseLong(str));
                    } catch (BusinessException e) {
                        LOGGER.info("changeDataMap4Upper4Number中查询枚举时报错，id : " + str);
                    }
                    if(item != null){
                        str = item.getEnumvalue();
                    }
                }
                if (str.contains(",")) {
                    str = str.replace(",", "");
                }
                if (str.contains("%")) {
                    str = str.replace("%", "");
                    str = String.valueOf(Double.parseDouble(str) / 100);
                }
                newValue = new BigDecimal(str);
            }else {
                hasNullField = true;
                dataMap.put("hasNullField",hasNullField);
            }
            dataMap.put(group1, newValue);
        }
        matcher.appendTail(newFormula);
    }

    @SuppressWarnings("unchecked")
    private static String toUpper(String str, boolean islong, int cnIndex) throws BusinessException {
        if (StringUtil.checkNull(str)) {
            return "";
        }
        if (str.indexOf(",") != -1) {
            str = str.replace(",", "");
        }
        //TODO 这里还存在百分号有问题的情况
        if (str.indexOf("%") != -1) {
            str = str.replace("%", "");
            str = String.valueOf(Double.parseDouble(str) / 100);
        }
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.putAll(formMap);
        changeDataMap4Upper4Number(str, dataMap);
        Boolean hasNullField = (Boolean) dataMap.get("hasNullField");
        if (hasNullField){
            //值为空不转换的新需求改动导致空字段放入groovy计算报错了(报错格式如：toUpperForShort( {数字} * 10000 ))
            //此处加个参数来判断，如果有空字段，直接跳出计算，不再执行大小写转换
            return "";
        }
        //OA-100748表单：表单设置了格式转换--调用提示error.将单引号替换
        str = str.replaceAll("\'\'","0").replaceAll("\'","");
        Object result = FormulaUtil.doResult(str, dataMap);
        //BUG_普通_V5_V6.0sp1_一星卡_福建匹克房地产开发有限公司惠安分公司_数字转换大写，如果为空不转换
        //新需求，值为空不转换
        if(result==null){
            return "";
        }
        BigDecimal val = new BigDecimal(result.toString());
        String cd = val.toPlainString();
        if (cd.indexOf(".") > 14 || (cd.indexOf(".") < 0 && cd.length() > 14)) {//整数部分大于14位
            LOGGER.warn("执行数字大小写转换时异常，需要转换的值超过支持的最大值，需要转换的值：" + str);
            return "";
        }
        double max_number = 999999999999999.99;

        String[][] digitsArray = new String[2][];
        String[] array1 = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] array2 = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        digitsArray[0] = array1;
        digitsArray[1] = array2;

        String[][] radicesArray = new String[2][];
        String[] radicesArray1 = {"", "拾", "佰", "仟"};
        String[] radicesArray2 = {"", "十", "百", "千"};
        radicesArray[0] = radicesArray1;
        radicesArray[1] = radicesArray2;

        String[][] bigRadicesArray = new String[2][];
        String[] bigRadicesArray1 = {"", "万", "亿", "万亿", "万万亿"};
        String[] bigRadicesArray2 = {"", "万", "亿", "万亿", "万万亿"};
        bigRadicesArray[0] = bigRadicesArray1;
        bigRadicesArray[1] = bigRadicesArray2;

        String[][] decimalsArray = new String[2][];
        String[] decimalsArray1 = {"角", "分", "整", "元", "元"};
        String[] decimalsArray2 = {"", "", "", "", "点"};
        decimalsArray[0] = decimalsArray1;
        decimalsArray[1] = decimalsArray2;

        // Stringiables:
        String integral; // Represent integral part of digit number.
        String decimal; // Represent decimal part of digit number.
        String outputCharacters; // The output result.
        String[] parts;
        int i = 0;
        // String zeroCount;
        String d;
        int p;
        int quotient;
        int modulus;

        // Validate input string:
        String currencyDigits = cd.toString();
        boolean negativesign = false;
        if (currencyDigits.substring(0, 1).indexOf("-") != -1) {
            negativesign = true;
            currencyDigits = currencyDigits.substring(1);
        }

        if (currencyDigits.matches("/[^,.\\d]/")) {
            throw new BusinessException("传入字符串不能转换为大写");
        }

        if ((currencyDigits).matches("/^((\\d{1,3}(,\\d{3})*(.((\\d{3},)*\\d{1,3}))?)|(\\d+(.\\d+)?))$/")) {
            throw new BusinessException("传入字符串不符合转换格式要求");
        }

        // Normalize the format of input digits:
        currencyDigits = currencyDigits.replaceAll(",", "");// Remove comma delimiters.
        currencyDigits = currencyDigits.replace("/^0+/", ""); // Trim zeros at the beginning.

        if (Double.parseDouble(currencyDigits) > max_number) {
            throw new BusinessException("需要转换的数值超过最大值");
        }

        if (currencyDigits.indexOf(".") != -1) {
            parts = currencyDigits.split("\\.");
            integral = parts[0];
            decimal = parts[1];
            // Cut down redundant decimal digits that are after the second.
            if (decimal.length() >= 2) {
                if (cnIndex != 1) {
                    decimal = decimal.substring(0, 2);
                }
            }
            if (Long.parseLong(decimal) == 0) {
                decimal = "";
            }
        } else {
            integral = currencyDigits;
            decimal = "";
        }

        String[] digits = digitsArray[cnIndex];
        String[] radices = radicesArray[cnIndex];
        String[] bigRadices = bigRadicesArray[cnIndex];
        String[] decimals = decimalsArray[cnIndex];
        int zeroCount;

        // Start processing:
        outputCharacters = "";
        // Process integral part if it is larger than 0:
        if (Long.parseLong(integral) > 0) {
            zeroCount = 0;
            for (int i1 = 0; i1 < integral.length(); i1++) {
                p = integral.length() - i1 - 1;
                d = integral.substring(i1, i1 + 1);
                quotient = p / 4;
                modulus = p % 4;
                if (!islong) {
                    if ("0".equals(d)) {
                        zeroCount++;
                    } else {
                        if (zeroCount > 0) {
                            outputCharacters += digits[0];
                        }
                        zeroCount = 0;
                        outputCharacters += digits[Integer.parseInt(d)] + radices[modulus];
                    }
                } else {
                    outputCharacters += digits[Integer.parseInt(d)] + radices[modulus];
                }
                if (modulus == 0
                        && (outputCharacters.charAt(outputCharacters.length() - 1)) != bigRadices[2].charAt(0)) {
                    outputCharacters += bigRadices[quotient];
                    zeroCount = 0;
                }
            }
            if (Strings.isBlank(decimal)) {
                outputCharacters += decimals[3];
            } else {
                outputCharacters += decimals[4];
            }
        }else if(Long.parseLong(integral) == 0){
            if (Strings.isBlank(decimal)) {
                outputCharacters += digits[0];
                if(cnIndex != 1){
                    outputCharacters += decimals[4];
                }
            } else {
                outputCharacters += digits[0] + decimals[4];
            }
        }

        boolean isInteger = true;
        // Process decimal part if there is:
        if (Strings.isNotBlank(decimal)) {
            d = decimal.substring(i, 1);
            for (int i1 = 0; i1 < decimal.length(); i1++) {
                d = decimal.substring(i1, i1 + 1);
                if (!"0".equals(d) || islong) {
                    outputCharacters += digits[Integer.parseInt(d)] + ((cnIndex != 1) ? decimals[i1] : "");
                } else if ("0".equals(d)) {
                    //原始条件：&& i1 == 1 OA-109969数字字段使用【中文小写】参与动态组合，结果显示不对，数字小数位中间的"0"没有读取到。
                    outputCharacters += digits[0] + ((cnIndex == 1) ? decimals[i1] : "");
                }
                //原始条件：&& i1 == 1OA-109969数字字段使用【中文小写】参与动态组合，结果显示不对，数字小数位中间的"0"没有读取到。
                if (!"0".equals(d) ) {
                    isInteger = false;
                }
            }
        }
        // Confirm and return the final output string:
        if (Strings.isBlank(outputCharacters)) {
            outputCharacters = digits[0] + decimals[3];
        }
        if (isInteger) {
            outputCharacters += decimals[2];
        }
        outputCharacters = "" + outputCharacters;
        if (negativesign) {
            outputCharacters = "负" + outputCharacters;
        }
        return outputCharacters;
    }

    /**
     * 获取指定小数位的DecimalFormat
     *
     * @param digitNum
     * @return
     */
    public static DecimalFormat getDecimalFormat(int digitNum, String formatStr) {
        String str = "";
        if (formatStr != null && !"".equals(formatStr)) {
            str = formatStr;
        } else {
            str = "#####0";
        }
        for (int i = 1; i <= digitNum; i++) {
            if (i == 1) {
                str = str + ".0";
            } else {
                str = str + "0";
            }
        }
        DecimalFormat decimalFmt = decimalFormats.get(str);
        if (null == decimalFmt) {
            decimalFmt = new DecimalFormat(str);
            //此处有个大坑，虽然遇到几率极小---客户bug编号：20171016045889
            //当服务器语言环境为某些国家时，DecimalFormat对象转换千分位后很可能小数点与千分位标点变化了。
            //此处直接指定小数点符号为'.'，指定千分位符号为','
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            decimalFmt.setDecimalFormatSymbols(symbols);
            decimalFormats.put(str, decimalFmt);
        }
        return decimalFmt;
    }

    /**
     * 获取格式化小数位后的值
     * @param digitNum
     * @param ft
     * @param v
     * @return
     */
    public static String getDigitNumFormatValue(String digitNum, String ft, BigDecimal v) {
        int digit = 0;
        BigDecimal value = v;
        if (value == null) {
            value = new BigDecimal("0");
        }
        if (digitNum != null && !digitNum.trim().isEmpty()) {
            digit = Integer.parseInt(digitNum);
        }
        String formatType = ft;
        if (!((value.doubleValue() > -1d && value.doubleValue() < 1d && digit > 1))) {
            formatType = StringUtils.replaceLast(formatType, "0", "");
        }
        String s = FormulaFunction.getDecimalFormat(digit, formatType).format(value);
        if (!StringUtil.checkNull(s) && s.indexOf(".") == 0) {
            s = "0" + s;
        }
        return s;
    }

    /**
     * 流水号计算函数
     *
     * @return 返回当前流水号当前值
     */
    public static Object serialNumber(String name) throws Exception {
        String sn = "";
        CAP4SerialNumberManager serialNumberManager = (CAP4SerialNumberManager) AppContext.getBean("cap4SerialNumberManager");
        if (Strings.isBlank(name)) {
            return "";
        }
        //通过线程变量获取当前计算的字段名称以及单据id
        String tempData = (String) AppContext.getThreadContext(FormConstant.serialCalFunctionKey);
        String[] tempArray = tempData != null && !"".equals(tempData) ? tempData.split("_") : null;
        String fieldName = "";
        Long dataId = 0L;
        Long formId = 0L;
        Long subId = 0L;
        if (tempArray != null && tempArray.length > 2) {
            fieldName = tempArray[0];
            formId = Long.parseLong(tempArray[1]);
            dataId = Long.parseLong(tempArray[2]);
            subId = Long.parseLong(tempArray[3]);
        }
        LOGGER.info("生成流水号 字段：" + fieldName + " 表单ID：" + formId + " 数据ID: " + dataId);
        //获取流水号值
        sn = serialNumberManager.getSerialCalValue(name, fieldName, dataId, formId, subId);
        if (StringUtil.checkNull(sn)) {
            LOGGER.info("本次没有生成流水号，流水号：" + name + " 字段：" + fieldName + " 表单ID：" + formId + " 数据ID: " + dataId);
            sn = "";
        }
        LOGGER.info("流水号：" + name + " " + sn);
        return sn;
    }

    /**
     * like函数实现
     *
     * @param f 字段域数据
     * @param s  匹配的字符串
     * @return
     * @throws BusinessException
     */
    public static boolean like(String f, String s) throws BusinessException {
        String field = f;
        String str = s;
        field = field == null ? null : field.toLowerCase();
        str = str == null ? null : str.toLowerCase();
        return field == null ? false : field.contains(str == null ? "" : str);
    }

    /**
     * not_like函数实现
     *
     * @param f 字段域数据
     * @param s   匹配的字符串
     * @return
     * @throws BusinessException
     */
    public static boolean not_like(String f, String s) throws BusinessException {
        String field = f;
        String str = s;
        field = field == null ? null : field.toLowerCase();
        str = str == null ? null : str.toLowerCase();
        return field == null ? true : !field.contains(str == null ? "" : str);
    }

    /**
     * 为null的函数
     *
     * @param field
     * @return
     * @throws BusinessException
     */
    public static boolean isNull(Object field) throws BusinessException {
        String fieldName = String.valueOf(field);
        Object val = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormBean form = (FormBean) formMap.get("formBean");
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            FormFieldBean fieldBean = form.getFieldBeanByName(fieldName);
            if (fieldBean != null) {//动态字段
                if (fieldBean.isMasterField()) {
                    val = cacheMasterData.getFieldValue(fieldName);
                } else {
                    formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey2);
                    if (formMap == null || !formMap.containsKey(fieldBean.getName())) {
                        formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
                    }
                    if (formMap.containsKey(fieldName)) {
                        FormDataSubBean subBean = null;
                        if (cacheMasterData != null && !StringUtil.checkNull(String.valueOf(formMap.get("id")))) {
                            subBean = cacheMasterData.getFormDataSubBeanById(fieldBean.getOwnerTableName(), Long.parseLong(String.valueOf(formMap.get("id"))));
                            if (subBean != null) {
                                val = subBean.getFieldValue(fieldName);//直接从缓存中取值来判断是否为空，因为通过计算map中取出来的值是加工处理过的，有可能空值被处理成了0
                            } else {
                                val = formMap.get(fieldName);
                            }
                        } else {
                            val = formMap.get(fieldName);
                        }
                    } else {
                        formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey2);
                        if (formMap == null) {
                            return false;
                        } else {
                            val = formMap.get(fieldName);
                        }
                    }
                }
                //前台组织机构如果没有选择的时候点击确定，前台返回的值是0，从而导致空判断有问题 详见OA-116837
                if(fieldBean.isOrg() && "0".equals(String.valueOf(val))){
                    return true;
                }
            } else {//非动态字段
                val = formMap.get(fieldName);
            }
        }

        if (val == null || Strings.isBlank(val.toString())) {
            return true;
        } else {
            if (val instanceof Number) {
                //哎 这个地方先把0也当成null吧
                //客户bug回测：20170621038808，员工—绩效季度管理表中，计算公式——动态组合设置中，设置条件重复行所有行=空值后不生效
                BigDecimal vb = new BigDecimal(val.toString());
                return vb.compareTo(new BigDecimal(0d))==0;
            }
            return false;
        }
    }

    /**
     * 非null函数
     *
     * @param field
     * @return
     * @throws BusinessException
     */
    public static boolean isNotNull(Object field) throws BusinessException {
        return !isNull(field);
    }

    /**
     * 返回一个数字列表中的最大值
     *
     * @param obj
     * @return
     */
    public static BigDecimal max(Object obj) {
        if (obj == null) {
            return new BigDecimal(0);
        }
        List<Number> numbers = new ArrayList<Number>();
        if (obj instanceof Number) {
            numbers.add((Number) obj);
        }
        if (obj instanceof List) {
            numbers.addAll((Collection<? extends Number>) obj);
        }
        if (Strings.isEmpty(numbers)) {
            return new BigDecimal(0);
        }
        return calcValByType(true, numbers);
    }

    /**
     * 返回一个数字列表中的最小值
     *
     * @param obj
     * @return
     */
    public static BigDecimal min(Object obj) {
        if (obj == null) {
            return new BigDecimal(0);
        }
        List<Number> numbers = new ArrayList<Number>();
        if (obj instanceof Number) {
            numbers.add((Number) obj);
        }
        if (obj instanceof List) {
            numbers.addAll((Collection<? extends Number>) obj);
        }
        if (Strings.isEmpty(numbers)) {
            return new BigDecimal(0);
        }
        return calcValByType(false, numbers);
    }

    /**
     * 返回一个数字列表中的最大值或者最小值
     *
     * @param isMax   为true返回最大值，isMax为false返回最小值
     * @param subVals
     * @return
     */
    public static BigDecimal calcValByType(boolean isMax, List<Number> subVals) {
        BigDecimal retVal = null;
        if (subVals == null || subVals.size() == 0) {
            retVal = null;
        } else {
            for (Number n : subVals) {
                if (n != null) {
                    if (retVal == null) {
                        retVal = new BigDecimal(n.toString());
                        continue;
                    }
                    if (isMax) {
                        retVal = retVal.max(new BigDecimal(n.toString()));
                    } else {
                        retVal = retVal.min(new BigDecimal(n.toString()));
                    }
                }
            }
        }
        return retVal;
    }

    public static List<Object> match(String fieldName, String condition) {
        return null;
    }

    public static BigDecimal getInt(Object obj) {
        //有的数字设置特别大，超过了int支持的最大限度，数字会溢出，这里改为string
        if (obj instanceof Number) {
            String num = obj.toString();
            if(num.contains(".")){
                num = num.substring(0,num.indexOf("."));
            }
            BigDecimal bd = new BigDecimal(num);
            return bd;
        }
        return new BigDecimal(0);
    }

    public static BigDecimal getMod(Object obj, Object obj2) {
        if (obj == null || obj2 == null) {
            return new BigDecimal(0);
        }
        //BUG_普通_V5_V5.1sp1_东阳光长江药业股份有限公司_数字类型的控件，当长度超过15位，取整数和取余数计算有误
        if (obj instanceof Number && obj2 instanceof Number) {
            String numberStr = obj.toString();
            BigDecimal number = new BigDecimal(numberStr);
            Number number2 = (Number) obj2;
            if(Double.doubleToRawLongBits(number2.doubleValue()) != 0){
                String number2Str = obj2.toString();
                BigDecimal number2Bd = new BigDecimal(number2Str);
                return number.divideAndRemainder(number2Bd)[1];
            }
        }
        return new BigDecimal(0);
    }

    public static BigDecimal year(Object obj) {
        return getYearDay(obj, 0);
    }

    public static BigDecimal month(Object obj) {
        return getYearDay(obj, 1);
    }

    public static BigDecimal day(Object obj) {
        return getYearDay(obj, 2);
    }

    public static BigDecimal weekday(Object obj) {
        return getYearDay(obj, 3);
    }

    private static BigDecimal getYearDay(Object o, int type) {
        if (o == null) {
            return new BigDecimal(0);
        }
        Object obj = o;
        if (obj instanceof String) {
            String new_name = (String) obj;
            obj = DateUtil.toDate(new_name, DateUtil.YEAR_MONTH_DAY_PATTERN);
        }
        if (obj instanceof Date) {
            Date new_name = (Date) obj;
            if (type == 0) { //年
                return new BigDecimal(DateUtil.getYear(new_name));
            } else if (type == 1) { // 月
                return new BigDecimal(DateUtil.getMonth(new_name));
            } else if (type == 2) { //日
                return new BigDecimal(DateUtil.getDay(new_name));
            } else if (type == 3) { //星期
                Calendar c = Calendar.getInstance();
                c.setTime(new_name);
                return new BigDecimal(c.get(Calendar.DAY_OF_WEEK) - 1);
            }
        }
        return new BigDecimal(0);
    }

    public static Date date(Object obj) {
        String dateStr = dateTime(obj, 1);
        if (Strings.isBlank(dateStr)) {
            return null;
        }
        return DateUtil.toDate(dateStr, DateUtil.YEAR_MONTH_DAY_PATTERN);
    }

    public static String time(Object obj) {
        return dateTime(obj, 2);
    }

    private static String dateTime(Object obj, int type) {
        if (obj == null) {
            return "";
        }
        if (obj instanceof String) {
            String new_name = (String) obj;
            String[] datetime = new_name.split(" ");
            if (type == 1) {
                return datetime[0];
            } else if (type == 2) {
                if (datetime.length < 2) {
                    return "";
                }
                return datetime[1];
            }
        }
        if (obj instanceof Date) {
            Date new_name = (Date) obj;
            if (type == 1) {
                return DateUtil.format(new_name, DateUtil.YEAR_MONTH_DAY_PATTERN);
            } else if (type == 2) {
                String value = DateUtil.format(new_name, DateUtil.HOUR_MINUTE_SECOND_PATTERN);
                value = value.substring(0, value.lastIndexOf(":"));
                return value;
            }
        }
        return "";
    }

    /**
     * 分组求和
     *
     * @param fieldName
     * @param condition
     * @return
     */
    public static BigDecimal sumif(String fieldName, String condition) {
        BigDecimal retVal = new BigDecimal(0);
        List<Number> param = getListVal(fieldName, condition);
        if (param != null && param.size() > 0) {
            retVal = sum(param);
        }
        return retVal;
    }

    /**
     * 分组求平均
     *
     * @param fieldName
     * @param condition
     * @return
     */
    public static BigDecimal averif(String fieldName, String condition) {
        BigDecimal retVal = new BigDecimal(0);
        List<Number> param = getListVal(fieldName, condition);
        if (param != null && param.size() > 0) {
            retVal = aver(param);
        }
        return retVal;
    }

    /**
     * 分类求最大值
     *
     * @param fieldName
     * @param condition
     * @return
     */
    public static BigDecimal maxif(String fieldName, String condition) {
        BigDecimal retVal = new BigDecimal(0);
        List<Number> param = getListVal(fieldName, condition);
        if (param != null && param.size() > 0) {
            retVal = calcValByType(true, param);
        }
        return retVal;
    }

    /**
     * 分类求最小值
     *
     * @param fieldName
     * @param condition
     * @return
     */
    public static BigDecimal minif(String fieldName, String condition) {
        BigDecimal retVal = new BigDecimal(0);
        List<Number> param = getListVal(fieldName, condition);
        if (param != null && param.size() > 0) {
            retVal = calcValByType(false, param);
        }
        return retVal;
    }

    /**
     * 获取满足条件的字段列表值
     *
     * @param fieldName
     * @param condition
     * @return
     */
    public static List<Number> getListVal(String fieldName, String condition) {

        List<Number> param = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            FormBean form = (FormBean) formMap.get("formBean");
            if (cacheMasterData != null && form != null) {
                param = new ArrayList<Number>();
                FormFieldBean field = form.getFieldBeanByName(fieldName);
                if (field == null) {
                    field = form.getFieldBeanByDisplay(fieldName);
                }
                List<FormDataSubBean> subDatas = cacheMasterData.getSubData(field.getOwnerTableName());
                try {
                    Map<String, Object> map = cacheMasterData.getFormulaMap(FormulaEnums.componentType_condition);
                    for (FormDataSubBean subData : subDatas) {
                        Map<String,Object> temp = new HashMap<String, Object>(map);
                        temp.putAll(subData.getFormulaMap(FormulaEnums.componentType_condition));
                        if (FormulaUtil.isMatch(condition, temp, FormulaFunction_contextKey2)) {
                            param.add(new BigDecimal(String.valueOf(subData.getFormulaMap(FormulaEnums.formulaType_number).get(field.getName()))));
                        }
                    }
                } catch (BusinessException e) {
                    LOGGER.error("行分类计算时，进行条件判断异常", e);
                }
            }
        }
        return param;
    }

    /**
     * 根据函数名称，通过反射方式执行自定义groovy 代码块
     *
     * @param functionName
     * @return
     */
    public static Object selfFunction(String functionName) {
        Object returnObject = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormBean form = (FormBean) formMap.get("formBean");
            FormDataBean dataBean = (FormDataBean) formMap.get("formDataBean");
            if (form == null || dataBean == null) {//预校验
                form = (FormBean) AppContext.getThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
                if (form != null) {
                    List<FormCustomFunction> customList = form.getCustomFunctionList();
                    FormCustomFunction resultCustom = null;
                    if (customList != null) {
                        for (FormCustomFunction customFunction : customList) {
                            if (customFunction.getFunctionName().equals(functionName)) {
                                resultCustom = customFunction;
                                break;
                            }
                        }
                    }
                    if (resultCustom != null && resultCustom.getReturnType() == FormulaEnums.CustomFunctionType.BOOLEAN.getKey()) {
                        return true;
                    }
                }
                return 0;//这里主要是预校验的时候用到，返回0是最合适的。
            }
            List<FormCustomFunction> customList = form.getCustomFunctionList();
            FormCustomFunction resultCustom = null;
            if (customList != null) {
                for (FormCustomFunction customFunction : customList) {
                    if (customFunction.getFunctionName().equals(functionName)) {
                        resultCustom = customFunction;
                        break;
                    }
                }
            }
            if (resultCustom != null) {
                try {
                    String codeText = " def " + functionName + "(Object[] param){" + resultCustom.getCodeText() + "}";
                    String functionParams = "";
                    //自定义函数可以不传任何参数的，此处防护一下参数为空报错
                    if (!("|").equals(resultCustom.getFunctionParam())){
                        functionParams = resultCustom.getFunctionParam().split("\\|")[0];
                    }
                    String[] paramArray = functionParams.split(",");
                    int returnType = resultCustom.getReturnType().intValue();
                    Object[] params = getParamObjectArray(formMap, form, dataBean, paramArray, returnType);
                    returnObject = FormulaUtil.doGroovyCode(functionName, codeText, params);
                    if (returnType == 0) {//表示返回数字类型
                        try {
                            if (returnObject != null) {
                                @SuppressWarnings("unused")
                                BigDecimal test = new BigDecimal(returnObject.toString());
                            }
                        } catch (Exception e) {
                            LOGGER.error("自定义函数转换数字类型异常：" + functionName, e);
                            returnObject = "";
                        }
                    } else if (returnType == 1) {
                        if (returnObject == null) {//如果返回值为空，给默认值""
                            returnObject = "";
                        }
                    } else if (returnType == 6) {
                        if (!(returnObject instanceof Boolean)) {
                            returnObject = false;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    LOGGER.error("表单id:"+form.getId() + " 表单名称："+ form.getFormName() + " 函数名称：" + functionName);
                }
            }
        }
        return returnObject;
    }

    /**
     * 根据缓存数据及参数获取groovy执行时的字段值数组。
     * @param formMap
     * @param form
     * @param dataBean
     * @param params
     * @param type
     * @return
     */
    private static Object[] getParamObjectArray(Map<String, Object> formMap, FormBean form, FormDataBean dataBean, String[] params, int type) {
        Object[] paramValue = null;
        FormDataSubBean subBean = (FormDataSubBean) formMap.get("subDataBean");
        if (params != null) {
            paramValue = new Object[params.length];
            String param = "";
            for (int i = 0; i < params.length; i++) {
                int j = params[i].indexOf(".");
                if (j > -1) {
                    param = params[i].substring(j + 1, params[i].length());
                } else {
                    param = params[i];
                }
                //这里对一些特别的值进行处理
                FormFieldBean ffb = form.getFieldBeanByName(param);
                Object value = formMap.get(param);
                if (ffb != null && value != null) {
                    try {
//    					Object[] resultValue = ffb.getDisplayValue(value);
//    					if(type == 0){//枚举数字就返回枚举值
//    						value = resultValue[2];
//    					}else{//枚举文本就返回枚举显示名称
//    						value = resultValue[1];
//    					}
                        if (FormFieldComEnum.CHECKBOX.getKey().equals(ffb.getFinalInputType())) {
                            if (!ffb.isMasterField() && subBean != null) {
                                value = subBean.getFieldValue(ffb.getName());
                            } else {
                                value = dataBean.getFieldValue(ffb.getName());
                            }
                        }
                    } catch (Exception e) {
                        paramValue[i] = value;
                    }
                    //当需要返回值为数字类型以及表单控件是数字类型的时候需要转换一下
                    //OA-112537自动化环境：表单文本字段设置了自定义函数，前端调用时始终返回空
                    //存在返回值为字符型，参数为数字型的时候会使用与的条件会引起GROOVY反射执行异常，将条件改为或。
                    if (FieldType.DECIMAL.getKey().equals(ffb.getFieldType()) || type == 0) {
                        try {
                            //这里try catch一下，上面条件改成或，value 有可能是非数字类型。
                            //BUG_紧急_V6.1SP1_三星卡_Groovy脚本中无法获取重复表参数_20171019046189_2017-10-19
                            //处理方案:如果传入参数是日期等非数字型数据，直接catch，保留原值，计算结果让自定义groovy脚本去决定。
                            value = new BigDecimal(value.toString());
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                //这里加上null是有局限性的。客户那代码块中文本类型返回值：可能传入的参数就永远不会为null了。
                if (value == null) {
                    value = "";
                }
                paramValue[i] = value;
                param = "";
            }
        }
        return paramValue;
    }

    public static boolean compareEnumValue(String field, String opr, String enumId) throws BusinessException {
        ConditionSymbol symbol = ConditionSymbol.getEnumByText(opr);
        if (symbol == null) {
            //不是条件运算符
            throw new BusinessException(ResourceUtil.getString("form.formula.engin.formula.check.enumcompare"));
        }
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap == null) {
            return false;
        }
        FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
        FormBean form = (FormBean) formMap.get("formBean");

        if (cacheMasterData == null || form == null) {
            return false;
        }

        String fieldName = field.trim();
        FormFieldBean fieldBean = form.getFieldBeanByName(fieldName);
        if (fieldBean == null) {
            //不是枚举字段
            throw new BusinessException(ResourceUtil.getString("form.formula.engin.formula.check.enumcompare"));
        }
        if (!fieldBean.isMasterField()) {
            formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey2);
            if (formMap == null || !formMap.containsKey(fieldBean.getName())) {
                formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
            }
        }

        if (StringUtil.checkNull(enumId)) {
            Object obj = formMap.get(fieldName);
            if (symbol == ConditionSymbol.equal) {
                return obj == null || "0".equals(obj.toString());
            }
            if (symbol == ConditionSymbol.notEqual) {
                return obj != null && !"0".equals(obj.toString()) && !"".equals(obj.toString());
            }
        } else {
            if (formMap.get(fieldName) == null && symbol == ConditionSymbol.equal) {
                return false;
            }
            if (formMap.get(fieldName) == null && symbol == ConditionSymbol.notEqual) {
                return true;
            }
        }
        if (formMap.get(fieldName) == null || StringUtil.checkNull(enumId)) {
            return false;
        }

        EnumManager manager = (EnumManager) AppContext.getBean("enumManagerNew");
        Long id1 = 0L;
        try {
            if(Strings.isBlank(formMap.get(fieldName).toString())){
                return false;
            }
            id1 = Long.valueOf(formMap.get(fieldName).toString());
        } catch (NumberFormatException e) {
            LOGGER.error("枚举比较函数中，转换字段值为枚举id时异常，比较结果判false, 传入的枚举值：" + fieldName + " = " + formMap.get(fieldName));
            return false;
        }
        CtpEnumItem item1 = manager.getCtpEnumItem(id1);
        CtpEnumItem item2 = manager.getCtpEnumItem(Long.parseLong(enumId));

        if (item1 == null || item2 == null) {
            return false;
        }
        if (fieldBean.getFieldType().equalsIgnoreCase(FieldType.VARCHAR.getKey())) {
            return (item1.getId().equals(item2.getId()) && symbol == ConditionSymbol.equal) || (!item1.getId().equals(item2.getId()) && symbol == ConditionSymbol.notEqual);
        } else if (fieldBean.getFieldType().equalsIgnoreCase(FieldType.DECIMAL.getKey())) {
        	//枚举比较只支持同层同级比较
            if (!(item1.getRefEnumid().equals(item2.getRefEnumid()) && item1.getParentId().equals(item2.getParentId()))) {
                if(symbol == ConditionSymbol.notEqual){
                    return true;
                }else{
                    return false;
                }
            }
            int obj1 = Integer.parseInt(item1.getEnumvalue());
            int obj2 = Integer.parseInt(item2.getEnumvalue());

            boolean result = false;
            switch (symbol) {
                case equal:
                    result = obj1 == obj2;
                    break;
                case notEqual:
                    result = obj1 != obj2;
                    break;
                case greatThan:
                    result = obj1 > obj2;
                    break;
                case greatAndEqual:
                    result = obj1 >= obj2;
                    break;
                case lessThan:
                    result = obj1 < obj2;
                    break;
                case lessAndEqual:
                    result = obj1 <= obj2;
                    break;
                default:

            }
            return result;
        } else {
            //字段类型错误
            throw new BusinessException(ResourceUtil.getString("form.formula.engin.formula.check.enumcompare"));
        }
    }

    private static boolean compare(Object feild1Value, Object feild2Value, String opr) throws BusinessException {
        String formula = feild1Value + " " + opr + " " + feild2Value;
        return FormulaUtil.isMatch(formula, new HashMap<String, Object>(), "compareEnumValue");
    }

    /**
     * 日期转化为大小写
     *
     * @param date
     * @return
     */
    public static String dateToUpper(Date date) {
        String res = "";
        if (date != null) {
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            int year = ca.get(Calendar.YEAR);
            int month = ca.get(Calendar.MONTH) + 1;
            int day = ca.get(Calendar.DAY_OF_MONTH);
            res = numToUpper(year) + "年" + monthToUppder(month) + "月" + dayToUppder(day) + "日";
        }
        return res;
    }

    /**
     * 将数字转化为大写
     *
     * @param num
     * @return
     */
    public static String numToUpper(int num) {
        // String u[] = {"零","壹","贰","叁","肆","伍","陆","柒","捌","玖"};
        String[] u = {"〇", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        char[] str = String.valueOf(num).toCharArray();
        String rstr = "";
        for (int i = 0; i < str.length; i++) {
            rstr = rstr + u[Integer.parseInt(str[i] + "")];
        }
        return rstr;
    }

    /**
     * 月转化为大写
     *
     * @param month
     * @return
     */
    public static String monthToUppder(int month) {
        if (month < 10) {
            return numToUpper(month);
        } else if (month == 10) {
            return "十";
        } else {
            return "十" + numToUpper(month - 10);
        }
    }

    /**
     * 日转化为大写
     *
     * @param day
     * @return
     */
    public static String dayToUppder(int day) {
        if (day < 20) {
            return monthToUppder(day);
        } else {
            char[] str = String.valueOf(day).toCharArray();
            if (str[1] == '0') {
                return numToUpper(Integer.parseInt(str[0] + "")) + "十";
            } else {
                return numToUpper(Integer.parseInt(str[0] + "")) + "十"
                        + numToUpper(Integer.parseInt(str[1] + ""));
            }
        }
    }

    /**
     * 获取某个日期是星期几
     */
    public static String getWeekDay(Calendar c) {
        String retVal;
        if (c == null) {
            retVal = "星期一";
        } else {
            switch (c.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    retVal = "星期一";
                    break;
                case Calendar.TUESDAY:
                    retVal = "星期二";
                    break;
                case Calendar.WEDNESDAY:
                    retVal = "星期三";
                    break;
                case Calendar.THURSDAY:
                    retVal = "星期四";
                    break;
                case Calendar.FRIDAY:
                    retVal = "星期五";
                    break;
                case Calendar.SATURDAY:
                    retVal = "星期六";
                    break;
                case Calendar.SUNDAY:
                    retVal = "星期日";
                    break;
                default:
                    retVal = "星期一";
            }
        }
        return retVal;
    }

    /**
     * f include v
     * include函数
     */
    public static boolean include(String f, String v) {
        String field = f == null ? null : f.toLowerCase();
        String value = v == null ? "" : v.toLowerCase();
        if (field == null) {
            //OA-92555 表单校验规则设置多组织控件包含单组织控件，都为空时校验结果为false
            if(Strings.isBlank(value)){
                return true;
            }
            return false;
        } else {
            if (value.contains(",")) {
                String[] strs = value.split(",");
                for (String str : strs) {
                    //需要全部都在，才为真
                    if (!field.contains(str)) {
                        return false;
                    }
                }
            } else {
                return field.contains(value);
            }
        }
        return true;
    }

    /**
     * f in v
     * in函数
     */
    public static boolean inner(String f, String v) {
        String field = f == null ? null : f.toLowerCase();
        String value = v == null ? "" : v.toLowerCase();
        if (field == null) {
            //OA-85150 校验规则设置in函数：A in B，A为空且B有值时不满足校验，应满足
            if (!Strings.isBlank(value)) {
                return true;
            }
            return false;
        } else {
            if (field.contains(",")) {
                String[] strs = field.split(",");
                for (String str : strs) {
                    //全部都在目标范围内才为真
                    if (!value.contains(str)) {
                        return false;
                    }
                }
            } else {
                //BUG_重要_V5_V6.0_深圳利亚德光电有限公司_信息管理点击某个记录的时候报错：您无权查看该主题
                String threadValue = (String) AppContext.getThreadContext("in_value"+value);
                if(Strings.isNotBlank(threadValue)){
                    Set<Long> deptIds = new HashSet<Long>();
                    OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
                    String[] ids = threadValue.split(",");
                    for(String id :ids){
                        deptIds.add(Long.valueOf(id));
                        if (!id.contains("|1")) {//包含子部门
                            try {
                                V3xOrgDepartment dept = orgManager.getDepartmentById(Long.parseLong(id));
                                if (dept != null) {
                                    List<V3xOrgDepartment> list = orgManager.getChildDepartments(dept.getId(), false);
                                    if (list != null && !list.isEmpty()) {
                                        int len = list.size();
                                        for (int i = 0; i < len; i++) {
                                            V3xOrgDepartment v3xOrgDepartment = list.get(i);
                                            deptIds.add(v3xOrgDepartment.getId());
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error("---getDepartmentById error---->", e);
                            }
                        }
                    }
                    return deptIds.contains(Long.valueOf(field));
                }else{
                    return value.contains(field);
                }
            }
        }
        return true;
    }

    /**
     * isRole函数
     *
     * @param memberId
     * @param roleCode
     * @param belongDept     主岗
     * @param secondDept     副岗
     * @param concurrentDept 兼职岗位
     * @return
     */
    public static boolean isRole(String memberId, String roleCode, boolean belongDept, boolean secondDept, boolean concurrentDept) throws BusinessException {
        boolean result = false;
        OrgManager orgManager = (OrgManager) AppContext.getBean("orgManager");
        if (!Strings.isBlank(roleCode)) {
            if (null == memberId) {
                return result;
            }
            V3xOrgMember member = orgManager.getMemberById(Long.valueOf(memberId));
            if (member == null) {
                return result;
            }
            ArrayList postTypes = new ArrayList();
            if (belongDept) {
                postTypes.add(MemberPostType.Main);
            }

            if (secondDept) {
                postTypes.add(MemberPostType.Second);
            }

            if (concurrentDept) {
                postTypes.add(MemberPostType.Concurrent);
            }

            if (postTypes.size() > 0) {
                Object baseAccountId = null;//member.getOrgAccountId();
                MemberPostType[] array = new MemberPostType[postTypes.size()];
                for(int i = 0,len = postTypes.size();i<len;i++){
                    array[i] = (MemberPostType)postTypes.get(i);
                }
                //如果new MemberPostType[]为空的话，说明不需要判断主岗、副岗、兼岗，只要有这个角色，就为true。
                //如果new MemberPostType[]不为空，则说明需要判断角色的岗位信息，就算找到角色，如果角色的岗位和人员的信息不对称，也为false；
                result = orgManager.isRole(Long.valueOf(memberId), (Long) baseAccountId, roleCode, array);

                //和孙老师沟通了，非主岗的意思是只要找到的角色不在主岗部门上就算true，所以这里需要再判断一次，调用我们自己写的方法。
                if(!result && secondDept){
                    result = FormulaFunctionUitl.isRole4NotMianPost(Long.valueOf(memberId), (Long) baseAccountId, roleCode);
                }
            }
        }
        return result;
    }

    /**
     * isNotRole函数
     *
     * @param memberId
     * @param roleCode
     * @param belongDept     主岗
     * @param secondDept     副岗
     * @param concurrentDept 兼职岗位
     * @return
     */
    public static boolean isNotRole(String memberId, String roleCode, boolean belongDept, boolean secondDept, boolean concurrentDept) throws BusinessException {
        if (Strings.isBlank(memberId) || Strings.isBlank(roleCode)) {
            return true;
        }
        return !isRole(memberId, roleCode, belongDept, secondDept, concurrentDept);
    }

    /**
     * 去掉日期差天小时分显示格式
     * add by chenxb 2015-12-03
     *
     * @param valueStr 被转换的字符串
     * @param digitNum 小数位数
     * @param type     显示类型 自然日：nature, 工作日：work
     */
    public static String convertFormatDayHourMinus(String valueStr, int digitNum, String type) {
        if (StringUtil.checkNull(valueStr)) {
            LOGGER.error("去掉日期差(xx天xx小时xx分)显示格式，被转换的字符串为null或空！");
            return "failure";
        }

        int indexD = valueStr.indexOf("天");
        int indexH = valueStr.indexOf("小时");
        int indexM = valueStr.indexOf("分");
        BigDecimal bd;
        if (!(indexD > -1 && indexH > -1 && indexM > -1)) {
            //进入此处说明导入的数据中没有格式，进行判断是否是正确的
            try {
                bd = new BigDecimal(valueStr);
            } catch (Exception e) {
                LOGGER.error("去掉日期差(xx天xx小时xx分)显示格式，被转换的字符串格式不正确！");
                return "failure";
            }
        } else {
            String dayStr = valueStr.substring(0, indexD);
            String hourStr = valueStr.substring(indexD + 1, indexH);
            String minuteStr = valueStr.substring(indexH + 2, indexM);
            //增加校验，判断天、小时、分中间输入的是否是整数,只要有一个不满足，返回0，且记录日志
            if (!(isDigits(dayStr) && isDigits(hourStr) && isDigits(minuteStr))) {
                LOGGER.error("去掉日期差(xx天xx小时xx分)显示格式，天、小时、分前面的字符串不是整数！");
                return "failure";
            }
            long day = Long.parseLong(dayStr);
            long hour = Long.parseLong(hourStr);
            long minute = Long.parseLong(minuteStr);
            //是数字且大于0小于24
            if (hour < 0 || hour >= 24) {
                LOGGER.error("去掉日期差(xx天xx小时xx分)显示格式，小时是负数或者大于等于24！");
                return "failure";
            }
            //是数字且大于0小于60
            if (minute < 0 || minute >= 60) {
                LOGGER.error("去掉日期差(xx天xx小时xx分)显示格式，分是负数或者大于等于60！");
                return "failure";
            }

            //获取计算基数
            double coefficientOfHour = 1.0;
            double coefficientOfMinute = 1.0;
            if (StringUtil.checkNull(type) || "nature".equals(type)) {
                coefficientOfHour = 24.0;//自然日一天是多少小时
                coefficientOfMinute = 60.0;//自然日一小时是多少分钟
            }
            if ("work".equals(type)) {
                coefficientOfHour = (getWorkTimeOfDay() / (60.0));//工作日一天是多少小时
                coefficientOfMinute = 60.0;//工作日一小时是多少分钟
            }

            String tmp = "0.0";
            if (day < 0 || hour < 0 || minute < 0) {
                tmp = "-" + (Math.abs(day) + Math.abs(hour / coefficientOfHour) + Math.abs(minute / coefficientOfHour / coefficientOfMinute));
            } else {
                tmp = (Math.abs(day) + Math.abs(hour / coefficientOfHour) + Math.abs(minute / coefficientOfHour / coefficientOfMinute)) + "";
            }
            bd = new BigDecimal(Double.parseDouble(tmp));
        }

        return getDecimalFormat(digitNum, "").format(bd);
    }

    /**
     * 去掉日期差显示格式，判断是否为整数
     */
    private static boolean isDigits(String str) {
        char[] chars = str.toCharArray();
        int start = (chars[0] == '-') ? 1 : 0;
        //"-"
        if (start == 1 && chars.length == 1) {
            return false;
        }
        for (int i = start; i < str.length(); i++) {
            if (!Character.isDigit(chars[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 去掉日期差天显示格式
     * add by chenxb 2015-12-03
     *
     * @param valueStr 被转换的字符串
     * @param digitNum 小数位数
     */
    public static String convertFormatDayMinus(String valueStr, int digitNum) {
        if (StringUtil.checkNull(valueStr)) {
            LOGGER.error("去掉日期差(xx天)显示格式，被转换的字符串为null或空！");
            return "failure";
        }
        BigDecimal bd;
        if (!valueStr.endsWith("天")) {
            //进入此处说明导入的数据中没有格式，进行判断是否是正确的
            try {
                bd = new BigDecimal(valueStr);
            } catch (Exception e) {
                LOGGER.error("去掉日期差(xx天)显示格式，被转换的字符串格式不正确！");
                return "failure";
            }
        } else {
            String dayStr = valueStr.substring(0, valueStr.length() - 2);
            if (!NumberUtils.isNumber(dayStr)) {
                LOGGER.error("去掉日期差(xx天)显示格式，天前面的字符串不是数字！");
                return "failure";
            }

            double day = Double.parseDouble(dayStr);
            bd = new BigDecimal(day);
        }

        return getDecimalFormat(digitNum, "").format(bd);
    }

    /**
     * 计算字段值的长度
     *
     * @param value
     * @return
     */
    public static int len(Object value) {
        String str = value == null?"":String.valueOf(value);
        if (Strings.isBlank(str)) {
            return 0;
        }
        int len = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            //中文字符按照3个字符长度算
            if (c <= 255) {
                len += 1;
            } else {
                len += 3;
            }
        }
        return len;
    }

    /**
     * 重复行取值-第一行
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName 值字段
     */
    public static Object getSubValueByFirst(String valueFieldName) {
        return getSubValueForFirstOrLast(valueFieldName, true);
    }

    /**
     * 重复行取值-最后一行
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName 值字段
     */
    public static Object getSubValueByLast(String valueFieldName) {
        return getSubValueForFirstOrLast(valueFieldName, false);
    }

    /**
     * 取第一行或最后一行的值字段的值
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName 值字段
     * @param isFirst        是否第一行
     */
    @SuppressWarnings("unchecked")
    private static Object getSubValueForFirstOrLast(String valueFieldName, boolean isFirst) {
        Object retVal = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            FormBean form = (FormBean) formMap.get("formBean");
            if (form == null) {
                form = (FormBean) AppContext.getThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
            }
            String valueType = "";//记录值字段类型
            if (form != null) {
                FormFieldBean valueField = form.getFieldBeanByName(valueFieldName);
                if (valueField == null) {
                    valueField = form.getFieldBeanByDisplay(valueFieldName);
                }
                valueType = valueField.getFieldType();
                if (cacheMasterData != null) {
                    List<FormDataSubBean> subBeans = cacheMasterData.getSubData(valueField.getOwnerTableName());
                    if (Strings.isNotEmpty(subBeans)) {
                        try {
                            String formulaType = FormulaEnums.getFormulaTypeByFieldType(valueField.getFieldType());
                            FormDataSubBean subBean;
                            if (isFirst) {
                                subBean = subBeans.get(0);
                            } else {
                                subBean = subBeans.get(subBeans.size() - 1);
                            }
                            retVal = subBean.getFormulaMap(formulaType).get(valueField.getName());
                        } catch (BusinessException e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
                if (retVal == null) {
                    if (valueField.getFieldType().equals(FieldType.DECIMAL.getKey())) {
                        retVal = new BigDecimal("0");
                    } else if (valueField.getFieldType().equals(FieldType.VARCHAR.getKey()) || valueField.getFieldType().equals(FieldType.LONGTEXT.getKey())) {
                        retVal = "";
                    }
                }
            }
            // 重复表行取值函数如果参与字符串动态组合则需要将其值转换为String类型的 add by chenxb 2016-04-25
            String resultFormulaType = formMap.get("formulaType") == null ? "" : (String) formMap.get("formulaType");
            if (!StringUtil.checkNull(resultFormulaType) && resultFormulaType.equals(FormulaEnums.formulaType_varchar)) {
                if (retVal == null) {
                    retVal = "";
                } else {
                    if (!StringUtil.checkNull(valueType) && valueType.equals(FieldType.TIMESTAMP.getKey())) {
                        retVal = DateUtil.format((Date) retVal);
                    } else if (!StringUtil.checkNull(valueType) && valueType.equals(FieldType.DATETIME.getKey())) {
                        retVal = DateUtil.formatDateTime((Date) retVal);
                    } else {
                        retVal = retVal.toString();
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * 重复行取值-重复表最大 返回 conditionFieldName 值最大的重复行的 valueFieldName 的值
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName     值字段
     * @param conditionFieldName 条件字段
     */
    public static Object getSubValueByMax(String valueFieldName, String conditionFieldName) {
        FormDataSubBean valueSubBean = getValueSubBean(conditionFieldName, "maxormin", true);
        return getValueFieldValue(valueFieldName, valueSubBean);
    }

    /**
     * 重复行取值-重复表最小 返回 conditionFieldName 值最小的重复行的 valueFieldName 的值
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName     值字段
     * @param conditionFieldName 条件字段
     */
    public static Object getSubValueByMin(String valueFieldName, String conditionFieldName) {
        FormDataSubBean valueSubBean = getValueSubBean(conditionFieldName, "maxormin", false);
        return getValueFieldValue(valueFieldName, valueSubBean);
    }

    /**
     * 重复行取值-重复表最早 返回 conditionFieldName 值最早的重复行的 valueFieldName 的值
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName     值字段
     * @param conditionFieldName 条件字段
     */
    public static Object getSubValueByEarliest(String valueFieldName, String conditionFieldName) {
        FormDataSubBean valueSubBean = getValueSubBean(conditionFieldName, "earliestorlatest", true);
        return getValueFieldValue(valueFieldName, valueSubBean);
    }

    /**
     * 重复行取值-重复表最早 返回 conditionFieldName 值最晚的重复行的 valueFieldName 的值
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName     值字段
     * @param conditionFieldName 条件字段
     */
    public static Object getSubValueByLatest(String valueFieldName, String conditionFieldName) {
        FormDataSubBean valueSubBean = getValueSubBean(conditionFieldName, "earliestorlatest", false);
        return getValueFieldValue(valueFieldName, valueSubBean);
    }

    /**
     * 返回条件字段值最大最小或最早最晚的重复行formDataSubBean 如果重复表最大最小或最早最晚的行有多个，自上而下，取满足条件的首行的值
     * add by chenxb 2016-01-12
     *
     * @param conditionFieldName 条件字段
     * @param flag               是否取最大或者最早
     */
    @SuppressWarnings("unchecked")
    private static FormDataSubBean getValueSubBean(String conditionFieldName, String funcType, boolean flag) {
        FormDataSubBean valueSubBean = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            FormBean form = (FormBean) formMap.get("formBean");
            if (cacheMasterData != null && form != null) {
                FormFieldBean conditionField = form.getFieldBeanByName(conditionFieldName);
                if (conditionField == null) {
                    conditionField = form.getFieldBeanByDisplay(conditionFieldName);
                }
                Object tempV = null;
                String formulaType = FormulaEnums.getFormulaTypeByFieldType(conditionField.getFieldType());
                List<FormDataSubBean> subBeans = cacheMasterData.getSubData(conditionField.getOwnerTableName());
                for (FormDataSubBean subBean : subBeans) {
                    try {
                        Object conditionValue = subBean.getFormulaMap(formulaType).get(conditionField.getName());
                        if (conditionValue != null) {
                            if (tempV == null) {
                                tempV = conditionValue;
                                valueSubBean = subBean;
                                continue;
                            }
                            //最大或最小
                            if ("maxormin".equals(funcType)) {
                                BigDecimal tmp = new BigDecimal(tempV.toString());
                                BigDecimal con = new BigDecimal(conditionValue.toString());
                                if (flag) {
                                    if (!tmp.equals(tmp.max(con))) {
                                        tempV = conditionValue;
                                        valueSubBean = subBean;
                                    }
                                } else {
                                    if (!tmp.equals(tmp.min(con))) {
                                        tempV = conditionValue;
                                        valueSubBean = subBean;
                                    }
                                }
                            }
                            //最早或最晚
                            else if ("earliestorlatest".equals(funcType)) {
                                if (flag) {
                                    if (compareDate("<", conditionValue, tempV)) {
                                        tempV = conditionValue;
                                        valueSubBean = subBean;
                                    }
                                } else {
                                    if (compareDate(">", conditionValue, tempV)) {
                                        tempV = conditionValue;
                                        valueSubBean = subBean;
                                    }
                                }
                            }
                        }
                    } catch (BusinessException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }

        return valueSubBean;
    }

    /**
     * 取重复行中对应字段的值
     * add by chenxb 2016-01-12
     *
     * @param valueFieldName 值字段
     * @param valueSubBean   重复行值对象
     */
    @SuppressWarnings("unchecked")
    private static Object getValueFieldValue(String valueFieldName, FormDataSubBean valueSubBean) {
        Object retVal = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            FormBean form = (FormBean) formMap.get("formBean");
            if (form == null) {
                form = (FormBean) AppContext.getThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
            }
            String valueType = "";//记录值字段类型
            if (form != null) {
                FormFieldBean valueField = form.getFieldBeanByName(valueFieldName);
                if (valueField == null) {
                    valueField = form.getFieldBeanByDisplay(valueFieldName);
                }
                valueType = valueField.getFieldType();
                if (cacheMasterData != null && valueSubBean != null) {
                    try {
                        String formulaType = FormulaEnums.getFormulaTypeByFieldType(valueField.getFieldType());
                        retVal = valueSubBean.getFormulaMap(formulaType).get(valueField.getName());
                    } catch (BusinessException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                //没有找到满足条件的值fieldbean或者只有一个空重复行时，如果是数字，则对其赋值0，如果是其他类型，则直接返回null
                if (retVal == null) {
                    if (valueField.getFieldType().equals(FieldType.DECIMAL.getKey())) {
                        retVal = new BigDecimal("0");
                    } else if (valueField.getFieldType().equals(FieldType.VARCHAR.getKey()) || valueField.getFieldType().equals(FieldType.LONGTEXT.getKey())) {
                        retVal = "";
                    }
                }
            }
            // 重复表行取值函数如果参与字符串动态组合则需要将其值转换为String类型的 add by chenxb 2016-04-25
            String resultFormulaType = formMap.get("formulaType") == null ? "" : (String) formMap.get("formulaType");
            if (!StringUtil.checkNull(resultFormulaType) && resultFormulaType.equals(FormulaEnums.formulaType_varchar)) {
                if (retVal == null) {
                    retVal = "";
                } else {
                    if (!StringUtil.checkNull(valueType) && valueType.equals(FieldType.TIMESTAMP.getKey())) {
                        retVal = DateUtil.format((Date) retVal);
                    } else if (!StringUtil.checkNull(valueType) && valueType.equals(FieldType.DATETIME.getKey())) {
                        retVal = DateUtil.formatDateTime((Date) retVal);
                    } else {
                        retVal = retVal.toString();
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * 与当前行相比较，返回conditions相等的离当前行最近的一行(向上)的field2字段的值
     * */
    public static Object preRow(String field1, String field2, String conditions) {
        Object returnValue = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormBean form = (FormBean) formMap.get("formBean");
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            if (form == null) {
                form = (FormBean) AppContext.getThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
            }
            if (form != null) {
                FormFieldBean fieldBean1 = form.getFieldBeanByName(field1);
                FormFieldBean fieldBean2 = form.getFieldBeanByName(field2);
                String formulaType = FormulaEnums.getFormulaTypeByFieldType(fieldBean2.getFieldType());
                if (cacheMasterData != null) {
                    List<FormDataSubBean> subBeans = cacheMasterData.getSubData(fieldBean2.getOwnerTableName());
                    if (subBeans != null) {
                        if (subBeans.size() == 1) {
                            if (fieldBean1.getFieldType().equals(FieldType.VARCHAR.getKey())) {
                                return "";
                            } else if (fieldBean1.getFieldType().equals(FieldType.DECIMAL.getKey())) {
                                return new BigDecimal(0);
                            }
                        } else {
                            try {
                                String[] conditionFieldNames = conditions.split("\\|");
                                List<FormDataSubBean> filterSubBeans = new ArrayList<FormDataSubBean>();
                                FormDataSubBean fdb = (FormDataSubBean) formMap.get("subDataBean");
                                for (FormDataSubBean sub : subBeans) {
                                    boolean isEquals = true;
                                    for (String fieldName : conditionFieldNames) {
                                        if (!String.valueOf(sub.getFieldValue(fieldName)).equals(String.valueOf(fdb.getFieldValue(fieldName)))) {//!String.valueOf(sub.getFormulaMap(formulaType).get(fieldName)).equals(String.valueOf(fdb.getFormulaMap(formulaType).get(fieldName)))
                                            isEquals = false;
                                            break;
                                        }
                                    }
                                    if (isEquals) {
                                        filterSubBeans.add(sub);
                                    }
                                }
                                for (int i = 1; i < filterSubBeans.size(); i++) {
                                    if (fdb.getId().equals(filterSubBeans.get(i).getId())) {
                                        FormDataSubBean preRowSubBean = filterSubBeans.get(i - 1);
                                        returnValue = preRowSubBean.getFormulaMap(formulaType).get(fieldBean2.getName());
                                        if (fieldBean1.getFieldType().equals(FieldType.VARCHAR.getKey())) {
                                            if (returnValue != null) {
                                                if (fieldBean2.getFieldType().equals(FieldType.TIMESTAMP.getKey())) {
                                                    returnValue = DateUtil.format((Date) returnValue);
                                                } else if (fieldBean2.getFieldType().equals(FieldType.DATETIME.getKey())) {
                                                    returnValue = DateUtil.formatDateTime((Date) returnValue);
                                                } else {
                                                    returnValue = returnValue.toString();
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }
                        }
                    }
                }
                //空值处理放到最后，所以的异常情况都走这个分支，从而保证返回值正确
                if (returnValue == null) {
                    if (fieldBean1.getFieldType().equals(FieldType.VARCHAR.getKey())) {
                        return "";
                    } else if (fieldBean1.getFieldType().equals(FieldType.DECIMAL.getKey())) {
                        return new BigDecimal(0);
                    }
                }
            }
        }
        return returnValue == null ? new BigDecimal(0) : returnValue;
    }

    /**
     * 返回当前行最近的一行(向上)的field2字段的值
     * */
    public static Object preRow(String field1, String field2) {
        Object returnValue = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey1);
        if (formMap != null) {
            FormBean form = (FormBean) formMap.get("formBean");
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            if (form == null) {
                form = (FormBean) AppContext.getThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
            }
            if (form != null) {
                FormFieldBean fieldBean1 = form.getFieldBeanByName(field1);
                FormFieldBean fieldBean2 = form.getFieldBeanByName(field2);
                String formulaType = FormulaEnums.getFormulaTypeByFieldType(fieldBean2.getFieldType());
                if (cacheMasterData != null) {
                    List<FormDataSubBean> subBeans = cacheMasterData.getSubData(fieldBean2.getOwnerTableName());
                    try {
                        FormDataSubBean fdb = (FormDataSubBean) formMap.get("subDataBean");
                        if (fdb != null) {
                            if (Strings.isNotEmpty(subBeans)) {
                                //如果是第一行的上一行，则直接返回处理，减少for循环
                                if(subBeans.size() == 1 && fdb.getId().equals(subBeans.get(0).getId())){
                                    if (fieldBean1.getFieldType().equals(FieldType.VARCHAR.getKey())) {
                                        return "";
                                    } else if (fieldBean1.getFieldType().equals(FieldType.DECIMAL.getKey())) {
                                        return new BigDecimal(0);
                                    }
                                }
                                for (int i = 1; i < subBeans.size(); i++) {
                                    if (fdb.getId().equals(subBeans.get(i).getId())) {
                                        FormDataSubBean preRowSubBean = subBeans.get(i - 1);
                                        returnValue = preRowSubBean.getFormulaMap(formulaType).get(fieldBean2.getName());
                                        if (fieldBean1.getFieldType().equals(FieldType.VARCHAR.getKey())) {
                                            if (returnValue != null) {
                                                if (fieldBean2.getFieldType().equals(FieldType.TIMESTAMP.getKey())) {
                                                    returnValue = DateUtil.format((Date) returnValue);
                                                } else if (fieldBean2.getFieldType().equals(FieldType.DATETIME.getKey())) {
                                                    returnValue = DateUtil.formatDateTime((Date) returnValue);
                                                } else {
                                                    returnValue = returnValue.toString();
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }else{
                            LOGGER.info("重复表上一行计算异常，未找到subDataBean，计算式：preRow(" + field1 + "," + field2 + ")");
                        }
                    } catch (Exception e) {
                        LOGGER.info("重复表上一行计算异常，计算式：preRow(" + field1 + "," + field2 + ")");
                        LOGGER.error(e.getMessage(), e);
                    }
                }else{
                    LOGGER.info("重复表上一行计算异常，未找到cacheMasterData，计算式：preRow(" + field1 + "," + field2 + ")");
                }
                //空值处理放到最后，所以的异常情况都走这个分支，从而保证返回值正确
                if (returnValue == null) {
                    if (fieldBean1.getFieldType().equals(FieldType.VARCHAR.getKey())) {
                        return "";
                    } else if (fieldBean1.getFieldType().equals(FieldType.DECIMAL.getKey())) {
                        return new BigDecimal(0);
                    }
                }
            }else{
                LOGGER.info("重复表上一行计算异常，未找到formbean，计算式：preRow(" + field1 + "," + field2 + ")");
            }
        }
        AppContext.removeThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
        return returnValue;
    }

    public static Object preRow(String field) {
        Object returnValue = null;
        Map<String, Object> formMap = (Map<String, Object>) AppContext.getThreadContext(FormulaFunction_contextKey2);
        if (formMap != null) {
            FormBean form = (FormBean) formMap.get("formBean");
            FormDataMasterBean cacheMasterData = (FormDataMasterBean) formMap.get("formDataBean");
            if (form == null) {
                form = (FormBean) AppContext.getThreadContext("EXPRESSION_FUNCTION_FORM_BEAN");
            }
            if (form != null) {
                FormFieldBean fieldBean = form.getFieldBeanByName(field);
                String formulaType = FormulaEnums.getFormulaTypeByFieldType(fieldBean.getFieldType());
                if (cacheMasterData != null) {
                    List<FormDataSubBean> subBeans = cacheMasterData.getSubData(fieldBean.getOwnerTableName());
                    if (subBeans != null) {
                        if (subBeans.size() > 1) {
                            /*try {
                                Long currDataId = Long.valueOf(String.valueOf(formMap.get("id")));
                                for (int i = 1; i < subBeans.size(); i++) {
                                    if (subBeans.get(i).getId().longValue() == currDataId.longValue()) {
                                        FormDataSubBean preRowSubBean = subBeans.get(i - 1);
                                        returnValue = preRowSubBean.getFormulaMap(formulaType).get(fieldBean.getName());
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                            }*/
                            returnValue = formMap.get(field+"_pre");
                        }
                    }
                } else {
                    return null;
                }
                /*if (fieldBean.getFieldType().equals(FieldType.DECIMAL.getKey()) && returnValue == null) {
                    returnValue = new BigDecimal(0);
                }*/
            }
        }
        return returnValue;
    }
    public static BigDecimal summarizeSum(Object objs){
         return sum(objs);
    }
    public static BigDecimal summarizeAver(Object objs){
        return aver(objs);
    }
    public static BigDecimal summarizeMax(Object objs){
        return max(objs);
    }
    public static BigDecimal summarizeMin(Object objs){
        return min(objs);
    }
}
