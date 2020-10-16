package com.seeyon;

public class test {
    public static void main(String[] args) {
        String s="1,2,3,4,6";
        String sub="4,";
        System.out.println(s.substring(0,s.indexOf(sub)+sub.length())+"5,"+s.substring(s.indexOf(sub)+sub.length()));
    }
}
