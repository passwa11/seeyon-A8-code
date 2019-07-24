package com.seeyon.apps.ext.Portal190724.dao;

import com.seeyon.apps.ext.Portal190724.pojo.Contract;

import java.util.List;
import java.util.Map;

public interface Portal190724Dao {

    int selectState(String s);

    Map select(String s);

    boolean save(Long long1, List<Contract> list);

    List<Contract> getAllLaw(Long long1);
}
