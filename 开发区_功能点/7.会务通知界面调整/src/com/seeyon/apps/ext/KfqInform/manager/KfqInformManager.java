package com.seeyon.apps.ext.KfqInform.manager;

import com.seeyon.apps.ext.KfqInform.po.KfqInform;

import java.util.List;
import java.util.Map;

public interface KfqInformManager {

    void saveInform(KfqInform inform);

    List<KfqInform> findInformbyUserid(Map map);
}
