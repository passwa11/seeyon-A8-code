package com.ncbi.medhub.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormater {

    public static LocalDate revokeString(String date) {
        StringBuffer sb = new StringBuffer();
        String[] s = date.split(" ");
        String year = s[0];
        String month = s[1];
        int day = Integer.parseInt(s[2]);
        String monthval = "";
        for (MonthEnum monthEnum : MonthEnum.values()) {
            if (monthEnum.name().equals(month)) {
                int integer = monthEnum.value;
                monthval = integer <= 9 ? "0" + integer : integer + "";
            }
        }
        String d = sb.append(year).append("-").append(monthval).append("-").append(day <= 9 ? "0" + day : day + "").toString();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(d, dtf);
        return localDate;
    }

    public enum MonthEnum {
        Jan(1), Feb(2),
        Mar(3), Apr(4),
        May(5), Jun(6),
        Jul(7), Aug(8),
        Sept(9), Oct(10),
        Nov(11), Dec(12);
        private int value;

        MonthEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
