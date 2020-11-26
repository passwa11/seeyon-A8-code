package com.seeyon.v3x.bulletin.dao;

import com.seeyon.v3x.bulletin.domain.EhSendRange;

import java.util.List;
import java.util.Map;

public interface EhSendRangeDao {

    void saveEhSendRange(EhSendRange en);

    void updateEhSendRange(EhSendRange en);

    List<EhSendRange> findEhSendRangeByCondition(Map moduleId);
}
