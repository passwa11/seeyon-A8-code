package com.test;

public class Test {
    public static void main(String[] args) {
        String url = "222.193.95.137:80/seeyon/collaboration/collaboration.do?method=summary&openFrom=listPending&affairId=7575228852124339003";
        System.out.println(url.substring(url.indexOf("seeyon")-1));
    }
}
