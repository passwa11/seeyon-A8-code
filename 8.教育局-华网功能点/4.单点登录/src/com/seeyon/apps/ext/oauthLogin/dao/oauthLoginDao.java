package com.seeyon.apps.ext.oauthLogin.dao;


import java.util.List;
import java.util.Map;

public interface oauthLoginDao {

    String selectLoginNameByCode(String code);

}