package com.seeyon.apps.customFieldCtrl.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CacheListData implements Serializable {

    private List<ZJsonObject> list=new ArrayList<>();

    public List<ZJsonObject> getList() {
        return list;
    }

    public void setList(List<ZJsonObject> list) {
        this.list = list;
    }
}
