package com.seeyon.apps.ext.Portal190724.manager;

import com.seeyon.apps.ext.Portal190724.po.Contract;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.FlipInfo;

import java.util.List;
import java.util.Map;

public interface Portal190724Manager {
    int selectState(String s);

    Map select(String s);

    boolean save(Long long1, List<Contract> list);

    List<Contract> getAllLaw(Long long1);

    List<Contract> getLimitLaw(Long long1);

    FlipInfo findMoreLaw(FlipInfo var1, Map var2) throws BusinessException;
}
