package com.monkeyk.sos.service;

import com.monkeyk.sos.domain.CheckUserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCheckUserStatus {

    @Autowired
    private RedisTemplate redisTemplate;


    public void save(CheckUserStatus status) {
        ValueOperations<String, CheckUserStatus> operations = redisTemplate.opsForValue();
        //设置缓存过期时间为30   单位：分钟　
        operations.set(status.getLoginname() + System.currentTimeMillis(), status, 30, TimeUnit.MINUTES);
    }

    public void delete(String loginName) {
        Set set = redisTemplate.keys(loginName + "*");
        List<String> list = new ArrayList<>(set);
        if (list.size() != 0) {
            for (String status : list) {
                redisTemplate.delete(status);
            }
        }
    }

    public List<CheckUserStatus> findAllByLoginname(String loginName) {
        Set set = redisTemplate.keys(loginName + "*");
        List<String> list = new ArrayList<>(set);
        List<CheckUserStatus> result = new ArrayList<>();
        if (list.size() != 0) {
            for (String status : list) {
                result.add((CheckUserStatus) redisTemplate.opsForValue().get(status));
            }
        }
        return result;
    }
}
