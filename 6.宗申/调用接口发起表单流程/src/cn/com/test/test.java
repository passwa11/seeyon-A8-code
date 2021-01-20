package cn.com.test;

import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        String info = "123456789|wwww";
        String[] arr = info.split("\\|");
        for (String s : arr) {
            System.out.println(s);
        }
        System.out.println(info.substring(0, 5));

        List<String> list = new ArrayList<>();
        list.add("2");
        list.add("2");
        list.add("2");
        String s=list.toString();
        System.out.println(s.substring(1,s.length()-1));
    }
}
