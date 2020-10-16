package com.seeyon.apps.customFieldCtrl.kit;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 结果集处理工具类
 * @author Yang.Yinghai
 * @date 2018年5月23日上午9:05:01
 * @Copyright Beijing Seeyon Software Co.,LTD
 */
public class StrKit {

    public static String str(Object o) {
        if(o == null) {
            return "";
        }
        if(o instanceof Date) {
            return DateKit.getSimpleDate((Date)o);
        }
        if(o instanceof String) {
            return (String)o;
        }
        return o.toString();
    }

    public static float toFloat(Object o) {
        if(o == null) {
            return 0f;
        } else if(o instanceof Float) {
            return (Float)o;
        } else if(o instanceof String) {
            return Float.valueOf((String)o);
        } else if(o instanceof BigDecimal) {
            return ((BigDecimal)o).floatValue();
        }
        return 0f;
    }

    public static Long toLong(Object o) {
        if(null == o) {
            return 0L;
        } else if(o instanceof Long) {
            return (Long)o;
        } else if(o instanceof String) {
            if("".equals(o)) {
                return 0L;
            }
            return Long.valueOf((String)o);
        } else if(o instanceof BigDecimal) {
            return ((BigDecimal)o).longValue();
        }
        return 0L;
    }

    /**
     * 取int值，为空返回0
     * @param obj 对象
     * @return
     */
    public static Integer toInteger(Object obj) {
        if(obj == null) {
            return 0;
        } else if(obj instanceof Long){
        	 return ((Long)obj).intValue();
        } else if(obj instanceof BigDecimal) {
            return ((BigDecimal)obj).intValue();
        }else if(obj instanceof String) {
           String o = (String) obj;
           if("".equals(o)) {
               return 0;
           } else {
               try {
                   return Integer.valueOf((String) obj);
               } catch(Exception e) {
                   return 0;
               }
           }
        } else if(obj instanceof Integer) {
        	return (Integer) obj;
        }
        return 0;
    }
    
    public static Date toDate(Object o) throws Exception {
        if(null == o) {
            return null;
        } else if(o instanceof Date) {
            return (Date)o;
        } else if(o instanceof Timestamp) {
            return ((Timestamp) o);
        } else if(o instanceof String){
        	return DateKit.getdayDate((String) o);
        }
        return null;
    }
    
    public static List<?> toList(Object o) {
        if(o == null) {
            return null;
        } else if(o instanceof List) {
            return (List<?>) o;
        } else {
            return null;
        }
    }
    
    /**
     * 判断对象是否为空
     * @param o
     * @return
     */
    public static boolean isNull(Object o) {
    	if(null == o) {
    		return true;
    	}
    	if(o instanceof String) {
    		return "".equals((String) o);
    	}
    	if(o instanceof Collection) {
    		// 集合数量为0 则为空
    		return ((Collection<?>) o).size() == 0;
    	}
    	return false;
    }
    
    /**
     * 删除最后一个字符
     * @param input
     * @return
     */
    public static String deleteLastChar(String input) {
        if(isNull(input))
            return "";
        return input.substring(0, input.length() - 1);

    }
    
    /**
     * 转换成  123,456,789的格式
     * @param inputString
     * @param length
     * @return
     */
    public static String getListStr(String inputString, int length) {
        if(isNull(inputString))
            return "";
        String str = "";
        List<String> strList = getStrList(inputString, length);
        for(String string : strList) {
            str += string + ",";
        }
        return deleteLastChar(str);
    }
    
    /**
     * 把原始字符串分割成指定长度的字符串列表
     * 
     * @param inputString
     *            原始字符串
     * @param length
     *            指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     * 
     * @param inputString
     *            原始字符串
     * @param length
     *            指定长度
     * @param size
     *            指定列表大小
     * @return
     */
    private static List<String> getStrList(String inputString, int length,
            int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     * 
     * @param str
     *            原始字符串
     * @param f
     *            开始位置
     * @param t
     *            结束位置
     * @return
     */
    private static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }
    
    public static String getFixedLengthStr(String str, int length) {
    	// 如果为空,返回全部都是0
    	if(length < 1) {
    		return "";
    	}
    	if(isNull(str)) {
    		return getZeor(length);
    	}
    	if(str.length() < length) {
    		return getZeor(length - str.length()) + str;
    	}
    	return str.substring(0, length);
    }
    
    private static String getZeor(int length) {
    	String str = "";
    	for(int i = 0; i < length; i++) {
    		str += "0";
    	}
    	return str;
    }
    
}
