package com.monkeyk.sos.domain;

import java.util.List;
import java.util.Map;

public interface CheckUserStatusDao {

    int addUserStatus(CheckUserStatus userStatus);

    void update(CheckUserStatus userStatus);

    List<CheckUserStatus> findAll(String loginName);

    void delete(String loginName);
}
