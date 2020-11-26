package com.test;

public class test {
    public static void main(String[] args) {
        String s="您好,您有1条待办事项[OA]研一:您好:您有4条待办事项!请及时处理[OA]请及时处理!!OA代办";
        System.out.println(s.substring(s.indexOf(":")+1));
    }
}
