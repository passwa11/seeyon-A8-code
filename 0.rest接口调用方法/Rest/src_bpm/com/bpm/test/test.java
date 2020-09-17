package com.bpm.test;

import com.bpm.TokenUtil;

public class test {
    public static void main(String[] args) {
        String token = TokenUtil.getToken();
        System.out.println(token);
    }
}
